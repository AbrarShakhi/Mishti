import Darwin
import Foundation
import ComposeApp

final class IosLlamaBridge: LlamaBridge {
    var isAvailable: Bool {
        !String(cString: llama_bridge_version()).isEmpty
    }

    func init_() { llama_bridge_init() }
    func version() -> String { String(cString: llama_bridge_version()) }
    func free() { llama_bridge_free() }

    func loadModel(path: String, contextTokens: Int32, threads: Int32) -> Int64 {
        path.withCString { llama_bridge_load_model($0, contextTokens, threads) }
    }

    func freeModel(handle: Int64) { llama_bridge_free_model(handle) }

    func chatTemplate(handle: Int64) -> String? {
        guard let template = llama_bridge_chat_template(handle) else { return nil }
        return String(cString: template)
    }

    func generate(
        handle: Int64,
        roles: [String],
        contents: [String],
        temperature: Float,
        topK: Int32,
        topP: Float,
        maxTokens: Int32,
        assistantPrefix: String,
        callback: @escaping (String) -> KotlinBoolean
    ) -> Int32 {
        guard roles.count == contents.count, !roles.isEmpty, roles.count <= Int(Int32.max) else { return -1 }
        let storedRoles = roles.map { strdup($0) }
        let storedContents = contents.map { strdup($0) }
        defer {
            storedRoles.forEach { Darwin.free($0) }
            storedContents.forEach { Darwin.free($0) }
        }
        guard storedRoles.allSatisfy({ $0 != nil }), storedContents.allSatisfy({ $0 != nil }) else { return -1 }
        let rolePointers = storedRoles.map { $0.map { UnsafePointer<CChar>($0) } }
        let contentPointers = storedContents.map { $0.map { UnsafePointer<CChar>($0) } }
        var pending = Data()
        typealias TokenConsumer = (UnsafePointer<CChar>) -> Bool
        var tokenCallback: TokenConsumer = { piece in
            let bytes = UnsafeRawPointer(piece).assumingMemoryBound(to: UInt8.self)
            pending.append(bytes, count: strlen(piece))
            guard let text = String(data: pending, encoding: .utf8) else { return true }
            pending.removeAll(keepingCapacity: true)
            return callback(text).boolValue
        }
        return withUnsafeMutablePointer(to: &tokenCallback) { context in
            assistantPrefix.withCString { prefix in
                rolePointers.withUnsafeBufferPointer { roleBuffer in
                    contentPointers.withUnsafeBufferPointer { contentBuffer in
                        llama_bridge_generate(
                            handle, roleBuffer.baseAddress, contentBuffer.baseAddress, Int32(roles.count),
                            temperature, topK, topP, maxTokens, prefix,
                            { piece, userData in
                                guard let piece = piece, let userData = userData else { return false }
                                let consume = userData.assumingMemoryBound(to: TokenConsumer.self).pointee
                                return consume(piece)
                            },
                            context
                        )
                    }
                }
            }
        }
    }
}
