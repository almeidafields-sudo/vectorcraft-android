# VectorCraft Native Lab — etapa B / Android ARM64

**Isto NÃO é o VectorCraft completo nem a interface egui portada.** É a primeira integração Android APK + Rust real, usando `vectorcraft-geom` do VectorCraft 0.11.0 para limitar coordenadas. Captura toque e S Pen (posição e pressão) numa tela de ensaio Kotlin Canvas. Assim isolamos toolchain, ABI, JNI e touch antes da parte difícil.

## Gerar APK (computador ou CI)

Requisitos: JDK 17, Android SDK API 35, NDK (ex. 27), Gradle 8.9, Rust >= 1.95 e cargo-ndk instalado (`cargo install cargo-ndk`). Certifique-se de que o `ANDROID_HOME` e `ANDROID_NDK_HOME` apontam para suas instalações.

Na **raiz do código VectorCraft modificado**: `bash android/build-native.sh`. O script gera `android/app/build/outputs/apk/debug/app-debug.apk`. O build depende da resolução das dependências Rust (incluindo `craft-libs`); não foi executado neste ambiente.

## Próximas etapas, não implementadas

1. Rodar o APK no Galaxy Tab S8 Ultra para validar carregamento JNI, coordenadas, pressão, orientação e tela cheia.
2. Migrar `eframe`/`egui` para uma atividade Android e integrar `VectorcraftApp` de `crates/ui-egui` em vez da tela de desenho provisória.
3. Implementar seleção de arquivos com Android Storage Access Framework, salvamento, fontes, teclado, atalhos DeX e ciclo de vida.
4. Integrar caneta (pressão/inclinação) com ferramentas internas do VectorCraft; testar desempenho de renderização e GPU.

O app protótipo usa `targetSdk=28` temporariamente para simplificar a primeira compilação. Não publique assim; ajuste alvo, políticas de armazenamento e ciclo de vida antes de distribuir.
