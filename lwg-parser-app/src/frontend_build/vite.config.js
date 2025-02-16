
import { defineConfig } from 'vite'

export default defineConfig({
    build: {
        outDir: '../main/resources/static/js/bundled',
        assetsDir: '',
        rollupOptions: {
            input: 'editor.js',
            output: {
                entryFileNames: 'editor.js',          // Fixed output filename without hashing
                chunkFileNames: '[name].js',          // Fixed names for chunks (if any)
                assetFileNames: '[name].[ext]',       // Fixed names for other assets (CSS, images)
            },
            preserveEntrySignatures: "allow-extension",
        },
    },
});