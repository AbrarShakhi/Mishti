import Foundation
import Shared

final class IosLlamaBridge: LlamaBridge {
    var isAvailable: Bool {
        let v = String(cString: llama_bridge_version())
        return v != "stub"
    }

    func init_() {
        llama_bridge_init()
    }

    func version() -> String {
        return String(cString: llama_bridge_version())
    }

    func free() {
        llama_bridge_free()
    }

    func loadModel(path: String, contextTokens: Int32, threads: Int32) -> Int64 {
        return path.withCString { cPath in
            llama_bridge_load_model(cPath, contextTokens, threads)
        }
    }

    func freeModel(handle: Int64) {
        llama_bridge_free_model(handle)
    }

    func chatTemplate(handle: Int64) -> String? {
        guard let cStr = llama_bridge_chat_template(handle) else { return nil }
        return String(cString: cStr)
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
        let count = Int32(roles.count)

        var roleCStrings = roles.map { strdup($0) }
        var contentCStrings = contents.map { strdup($0) }

        defer {
            for ptr in roleCStrings { free(ptr) }
            for ptr in contentCStrings { free(ptr) }
        }

        var box: (String) -> Bool = { token in
            callback(token).boolValue
        }

        return assistantPrefix.withCString { cPrefix in
            roleCStrings.withUnsafeMutableBufferPointer { rBuf in
                contentCStrings.withUnsafeMutableBufferPointer { cBuf in
                    llama_bridge_generate(
                        handle,
                        rBuf.baseAddress.map { UnsafePointer($0) },
                        cBuf.baseAddress.map { UnsafePointer($0) },
                        count,
                        temperature,
                        topK,
                        topP,
                        maxTokens,
                        cPrefix,
                        { piece, userData in
                            guard let piece = piece, let userData = userData else { return true }
                            let closure = userData.assumingMemoryBound(to: ((String) -> Bool).self).pointee
                            let token = String(cString: piece)
                            return closure(token)
                        },
                        &box
                    )
                }
            }
        }
    }
}
