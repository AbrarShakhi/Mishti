import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        LlamaBridgeRegistry.shared.register(bridge: IosLlamaBridge())
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
