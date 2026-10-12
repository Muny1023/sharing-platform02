// DEV is compiled to false in production, even when the custom flag is set.
export const offlinePreview = import.meta.env.DEV && import.meta.env.VITE_OFFLINE_PREVIEW === 'true'
