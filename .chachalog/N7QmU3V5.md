---
# Allowed version bumps: patch, minor, major
jahia-private: minor
---

## Image Service Migration to OSGi Bundle

### Deprecated Classes
- **`AbstractImageService`** - All methods now delegate to OSGi image service bundle
- **`AbstractJava2DImageService`** - Removed all implementation logic, delegates to OSGi service
- **`BufferImage`** - Marked as deprecated
- **`ImageJImage`** - Marked as deprecated
- **`ImageMagickImage`** - Marked as deprecated
- **`ImageJImageService`** - Marked as deprecated, delegates to OSGi service
- **`ImageJAndJava2DImageService`** - Marked as deprecated, delegates to OSGi service
- **`ImageMagickImageService`** - Marked as deprecated, delegates to OSGi service
- **`ImageMagickImage6Service`** - Marked as deprecated, delegates to OSGi service
- **`Java2DProgressiveBilinearImageService`** - Marked as deprecated, delegates to OSGi service

All deprecated classes are marked with `@Deprecated(since = "8.2.4.0", forRemoval = true)` and recommend using the OSGi service from `org.jahia.bundles.core.services.images`.

### Core Changes
- **`AbstractImageService`** - Now delegates all operations (`createThumb`, `resizeImage`, `getImage`, `getHeight`, `getWidth`, `cropImage`, `rotateImage`) to OSGi service via `BundleUtils.getOsgiService()`
- Removed implementation code for image manipulation operations
- Added `UnsupportedOperationException` when OSGi service is unavailable

### Spring Configuration
- **`applicationcontext-services.xml`** - Added deprecation comments to image service bean definitions
- Marked all image service beans as deprecated with a recommendation to use OSGi service
