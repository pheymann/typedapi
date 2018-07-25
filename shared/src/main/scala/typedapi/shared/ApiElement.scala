package typedapi.shared

import scala.annotation.implicitNotFound

sealed trait ApiElement

/** Type-container providing the singleton-type of an static path element */
sealed trait PathElement[P]

/** Type-container providing the name (singleton) and value type for a path parameter. */
sealed trait SegmentParam[K, V] extends ApiElement

/** Type-container providing the name (singleton) and value type for a query parameter. */
sealed trait QueryParam[K, V] extends ApiElement

/** Type-container providing the name (singleton) and value type for a header parameter. */
sealed trait HeaderParam[K, V] extends ApiElement

/** Type-container providing the name (singleton) and value type for a static header element. */
sealed trait FixedHeaderElement[K, V] extends ApiElement
/** Type-container providing the name (singleton) and value type for a static header element only used solely for the client. */
sealed trait ClientHeaderElement[K, V] extends ApiElement
/** Type-container providing the name (singleton) and value type for a static header element only used solely for the server. */
sealed trait ServerHeaderElement[K, V] extends ApiElement

/** Type-container providing the media-type and value type for a request body. */
sealed trait ReqBodyElement[MT <: MediaType, A] extends ApiElement

trait MethodElement extends ApiElement
/** Type-container representing a GET operation with a media-type and value type for the result. */
sealed trait GetElement[MT <: MediaType, A] extends MethodElement
/** Type-container representing a PUT operation with a media-type and value type for the result. */
sealed trait PutElement[MT <: MediaType, A] extends MethodElement
/** Type-container representing a PUT operation with a media-type and value type for the result and a body. */
sealed trait PutWithBodyElement[BMT <: MediaType, Bd, MT <: MediaType, A] extends MethodElement
/** Type-container representing a POST operation with a media-type and value type for the result. */
sealed trait PostElement[MT <: MediaType, A] extends MethodElement
/** Type-container representing a POST operation with a media-type and value type for the result and a body. */
sealed trait PostWithBodyElement[BMT <: MediaType, Bd, MT <: MediaType, A] extends MethodElement
/** Type-container representing a DELETE operation with a media-type and value type for the result. */
sealed trait DeleteElement[MT <: MediaType, A] extends MethodElement

@implicitNotFound("""You try to add a request body to a method which doesn't expect one.

method: ${M}
""")
trait MethodToReqBody[M <: MethodElement, MT <: MediaType, Bd] {

  type Out <: MethodElement
}

trait MethodToReqBodyLowPrio {

  implicit def reqBodyForPut[MT <: MediaType, A, BMT <: MediaType, Bd] = new MethodToReqBody[PutElement[MT, A], BMT, Bd] {
    type Out = PutWithBodyElement[BMT, Bd, MT, A]
  }

  implicit def reqBodyForPost[MT <: MediaType, A, BMT <: MediaType, Bd] = new MethodToReqBody[PostElement[MT, A], BMT, Bd] {
    type Out = PostWithBodyElement[BMT, Bd, MT, A]
  }
}

trait MediaType { self =>
  def value: String
}

trait MediaTypes {

  case object NoMediaType extends MediaType {
    val value = "NO MEDIA TYPE"
  }

  case object `application/atom+xml`                                                      extends MediaType {
    val value = "application/atom+xml"
  }
  type `Application/atom+xml` = `application/atom+xml`.type                                                     
  case object `application/base64`                                                        extends  MediaType {
    val value = "application/base64"
  }
  type `Application/base64` = `application/base64`.type                                               
  case object `application/excel`                                                         extends MediaType {
    val value = "application/excel"
  }
  type `Application/excel` = `application/excel`.type                                                         
  case object `application/font-woff`                                                     extends MediaType {
    val value = "application/font-woff"
  }
  type `Application/font-woff` = `application/font-woff`.type                                                     
  case object `application/gnutar`                                                        extends MediaType {
    val value = "application/gnutar"
  }
  type `Application/gnutar` = `application/gnutar`.type                                                        
  case object `application/java-archive`                                                  extends MediaType {
    val value = "application/java-archive"
  }
  type `Application/java-archive` = `application/java-archive`.type                                                  
  case object `application/javascript`                                                    extends MediaType {
    val value = "application/javascript"
  }
  type `Application/javascript` = `application/javascript`.type                                                    
  case object `application/json`                                                          extends MediaType {
    val value = "application/json"
  }
  type `Application/json` = `application/json`.type                                                          
  case object `application/json-patch+json`                                               extends MediaType {
    val value = "application/json-path+json"
  }
  type `Application/json-patch+json` = `application/json-patch+json`.type                                               
  case object `application/grpc+proto`                                                    extends MediaType {
    val value = "application/grpc+proto"
    type `Application/grpc+proto` = `application/grpc+proto`.type                                                    
  }
  case object `application/lha`                                                           extends MediaType {
    val value = "application/lha"
  }
  type `Application/lha` = `application/lha`.type                                                           
  case object `application/lzx`                                                           extends MediaType {
    val value = "application/lzx"
  }
  type `Application/lzx` = `application/lzx`.type                                                           
  case object `application/mspowerpoint`                                                  extends MediaType {
    val value = "application/mspowerpoint"
  }
  type `Application/mspowerpoint` = `application/mspowerpoint`.type                                                  
  case object `application/msword`                                                        extends MediaType {
    val value = "application/msword"
  }
  type `Application/msword` = `application/msword`.type                                                        
  case object `application/octet-stream`                                                  extends MediaType {
    val value = "application/octet-stream"
  }
  type `Application/octet-stream` = `application/octet-stream`.type                                                  
  case object `application/pdf`                                                           extends MediaType {
    val value = "application/pdf"
  }
  type `Application/pdf` = `application/pdf`.type                                                           
  case object `application/postscript`                                                    extends MediaType {
    val value = "application/postscript"
  }
  type `Application/postscript` = `application/postscript`.type                                                    
  case object `application/rss+xml`                                                       extends MediaType {
    val value = "application/rss+xml"
  }
  type `Application/rss+xml` = `application/rss+xml`.type                                                       
  case object `application/soap+xml`                                                      extends MediaType {
    val value = "application/soap+xml"
  }
  type `Application/soap+xml` = `application/soap+xml`.type                                                      
  case object `application/vnd.api+json`                                                  extends MediaType {
    val value = "application/vnd.api+json"
  }
  type `Application/vnd.api+json` = `application/vnd.api+json`.type                                                  
  case object `application/vnd.google-earth.kml+xml`                                      extends MediaType {
    val value = "application/vnd.google-earth.kml+xml"
  }
  type `Application/vnd.google-earth.kml+xml` = `application/vnd.google-earth.kml+xml`.type                                      
  case object `application/vnd.google-earth.kmz`                                          extends MediaType {
    val value = "application/vnd.google-earth.kmz"
  }
  type `Application/vnd.google-earth.kmz` = `application/vnd.google-earth.kmz`.type                                          
  case object `application/vnd.ms-fontobject`                                             extends MediaType {
    val value = "application/vnd.ms-fontobject"
  }
  type `Application/vnd.ms-fontobject` = `application/vnd.ms-fontobject`.type                                             
  case object `application/vnd.oasis.opendocument.chart`                                  extends MediaType {
    val value = "application/vnd.oasis.opendocument.chart"
  }
  type `Application/vnd.oasis.opendocument.chart` = `application/vnd.oasis.opendocument.chart`.type                                  
  case object `application/vnd.oasis.opendocument.database`                               extends MediaType {
    val value = "application/vnd.oasis.opendocument.database"
  }
  type `Application/vnd.oasis.opendocument.database` = `application/vnd.oasis.opendocument.database`.type                               
  case object `application/vnd.oasis.opendocument.formula`                                extends MediaType {
    val value = "application/vnd.oasis.opendocument.formula"
  }
  type `Application/vnd.oasis.opendocument.formula` = `application/vnd.oasis.opendocument.formula`.type                                
  case object `application/vnd.oasis.opendocument.graphics`                               extends MediaType {
    val value = "application/vnd.oasis.opendocument.graphics"
  }
  type `Application/vnd.oasis.opendocument.graphics` = `application/vnd.oasis.opendocument.graphics`.type                               
  case object `application/vnd.oasis.opendocument.image`                                  extends MediaType {
    val value = "application/vnd.oasis.opendocument.image"
  }
  type `Application/vnd.oasis.opendocument.image` = `application/vnd.oasis.opendocument.image`.type                                  
  case object `application/vnd.oasis.opendocument.presentation`                           extends MediaType {
    val value = "application/vnd.oasis.opendocument.presentation"
  }
  type `Application/vnd.oasis.opendocument.presentation` = `application/vnd.oasis.opendocument.presentation`.type                           
  case object `application/vnd.oasis.opendocument.spreadsheet`                            extends MediaType {
    val value = "application/vnd.oasis.opendocument.spreadsheet"
  }
  type `Application/vnd.oasis.opendocument.spreadsheet` = `application/vnd.oasis.opendocument.spreadsheet`.type                            
  case object `application/vnd.oasis.opendocument.text`                                   extends MediaType {
    val value = "application/vnd.oasis.opendocument.text"
  }
  type `Application/vnd.oasis.opendocument.text` = `application/vnd.oasis.opendocument.text`.type                                   
  case object `application/vnd.oasis.opendocument.text-master`                            extends MediaType {
    val value = "application/vnd.oasis.opendocument.text-master"
  }
  type `Application/vnd.oasis.opendocument.text-master` = `application/vnd.oasis.opendocument.text-master`.type                            
  case object `application/vnd.oasis.opendocument.text-web`                               extends MediaType {
    val value = "application/vnd.oasis.opendocument.text-web"
  }
  type `Application/vnd.oasis.opendocument.text-web` = `application/vnd.oasis.opendocument.text-web`.type                               
  case object `application/vnd.openxmlformats-officedocument.presentationml.presentation` extends MediaType {
    val value = "application/vnd.openxmlformats-officedocument.presentationml.presentation"
  }
  type `Application/vnd.openxmlformats-officedocument.presentationml.presentation` = `application/vnd.openxmlformats-officedocument.presentationml.presentation`.type 
  case object `application/vnd.openxmlformats-officedocument.presentationml.slide`        extends MediaType {
    val value = "application/vnd.openxmlformats-officedocument.presentationml.slide"
  }
  type `Application/vnd.openxmlformats-officedocument.presentationml.slide` = `application/vnd.openxmlformats-officedocument.presentationml.slide`.type        
  case object `application/vnd.openxmlformats-officedocument.presentationml.slideshow`    extends MediaType {
    val value = "application/vnd.openxmlformats-officedocument.presentationml.slideshow"
  }
  type `Application/vnd.openxmlformats-officedocument.presentationml.slideshow` = `application/vnd.openxmlformats-officedocument.presentationml.slideshow`.type    
  case object `application/vnd.openxmlformats-officedocument.presentationml.template`     extends MediaType {
    val value = "application/vnd.openxmlformats-officedocument.presentationml.template"
  }
  type `Application/vnd.openxmlformats-officedocument.presentationml.template` = `application/vnd.openxmlformats-officedocument.presentationml.template`.type     
  case object `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`         extends MediaType {
    val value = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
  }
  type `Application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` = `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`.type         
  case object `application/vnd.openxmlformats-officedocument.spreadsheetml.template`      extends MediaType {
    val value = "application/vnd.openxmlformats-officedocument.spreadsheetml.template"
  }
  type `Application/vnd.openxmlformats-officedocument.spreadsheetml.template` = `application/vnd.openxmlformats-officedocument.spreadsheetml.template`.type      
  case object `application/vnd.openxmlformats-officedocument.wordprocessingml.document`   extends MediaType {
    val value = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
  }
  type `Application/vnd.openxmlformats-officedocument.wordprocessingml.document` = `application/vnd.openxmlformats-officedocument.wordprocessingml.document`.type   
  case object `application/vnd.openxmlformats-officedocument.wordprocessingml.template`   extends MediaType {
    val value = "application/vnd.openxmlformats-officedocument.wordprocessingml.template"
  }
  type `Application/vnd.openxmlformats-officedocument.wordprocessingml.template` = `application/vnd.openxmlformats-officedocument.wordprocessingml.template`.type   
  case object `application/x-7z-compressed`                                               extends MediaType {
    val value = "application/x-7z-compressed"
  }
  type `Application/x-7z-compressed` = `application/x-7z-compressed`.type                                               
  case object `application/x-ace-compressed`                                              extends MediaType {
    val value = "application/x-ace-compressed"
  }
  type `Application/x-ace-compressed` = `application/x-ace-compressed`.type                                              
  case object `application/x-apple-diskimage`                                             extends MediaType {
    val value = "application/x-apple-diskimage"
  }
  type `Application/x-apple-diskimage` = `application/x-apple-diskimage`.type                                             
  case object `application/x-arc-compressed`                                              extends MediaType {
    val value = "application/x-arc-compressed"
  }
  type `Application/x-arc-compressed` = `application/x-arc-compressed`.type                                              
  case object `application/x-bzip`                                                        extends MediaType {
    val value = "application/x-bzip"
  }
  type `Application/x-bzip` = `application/x-bzip`.type                                                        
  case object `application/x-bzip2`                                                       extends MediaType {
    val value = "application/x-bzip2"
  }
  type `Application/x-bzip2` = `application/x-bzip2`.type                                                       
  case object `application/x-chrome-extension`                                            extends MediaType {
    val value = "application/x-chrome-extension"
  }
  type `Application/x-chrome-extension` = `application/x-chrome-extension`.type                                            
  case object `application/x-compress`                                                    extends MediaType {
    val value = "application/x-compress"
  }
  type `Application/x-compress` = `application/x-compress`.type                                                    
  case object `application/x-compressed`                                                  extends MediaType {
    val value = "application/x-compressed"
  }
  type `Application/x-compressed` = `application/x-compressed`.type                                                  
  case object `application/x-debian-package`                                              extends MediaType {
    val value = "application/x-debian-package"
  }
  type `Application/x-debian-package` = `application/x-debian-package`.type                                              
  case object `application/x-dvi`                                                         extends MediaType {
    val value = "application/x-dvi"
  }
  type `Application/x-dvi` = `application/x-dvi`.type                                                         
  case object `application/x-font-truetype`                                               extends MediaType {
    val value = "application/x-font-truetype"
  }
  type `Application/x-font-truetype` = `application/x-font-truetype`.type                                               
  case object `application/x-font-opentype`                                               extends MediaType {
    val value = "application/x-font-opentype"
  }
  type `Application/x-font-opentype` = `application/x-font-opentype`.type                                               
  case object `application/x-gtar`                                                        extends MediaType {
    val value = "application/x-gtar"
  }
  type `Application/x-gtar` = `application/x-gtar`.type                                                        
  case object `application/x-gzip`                                                        extends MediaType {
    val value = "application/x-gzip"
  }
  type `Application/x-gzip` = `application/x-gzip`.type                                                        
  case object `application/x-latex`                                                       extends MediaType {
    val value = "application/x-latex"
  }
  type `Application/x-latex` = `application/x-latex`.type                                                       
  case object `application/x-rar-compressed`                                              extends MediaType {
    val value = "application/x-rar-compressed"
  }
  type `Application/x-rar-compressed` = `application/x-rar-compressed`.type                                              
  case object `application/x-redhat-package-manager`                                      extends MediaType {
    val value = "application/x-redhat-package-manager"
  }
  type `Application/x-redhat-package-manager` = `application/x-redhat-package-manager`.type                                      
  case object `application/x-shockwave-flash`                                             extends MediaType {
    val value = "application/x-shockwave-flash"
  }
  type `Application/x-shockwave-flash` = `application/x-shockwave-flash`.type                                             
  case object `application/x-tar`                                                         extends MediaType {
    val value = "application/x-tar"
  }
  type `Application/x-tar` = `application/x-tar`.type                                                         
  case object `application/x-tex`                                                         extends MediaType {
    val value = "application/x-tex"
  }
  type `Application/x-tex` = `application/x-tex`.type                                                         
  case object `application/x-texinfo`                                                     extends MediaType {
    val value = "application/x-texinfo"
  }
  type `Application/x-texinfo` = `application/x-texinfo`.type                                                     
  case object `application/x-vrml`                                                        extends MediaType {
    val value = "application/x-vrml"
  }
  type `Application/x-vrml` = `application/x-vrml`.type                                                        
  case object `application/x-www-form-urlencoded`                                         extends MediaType {
    val value = "application/x-www-form-urlencoded"
  }
  type `Application/x-www-form-urlencoded` = `application/x-www-form-urlencoded`.type                                         
  case object `application/x-x509-ca-cert`                                                extends MediaType {
    val value = "application/x-x509-ca-cert"
  }
  type `Application/x-x509-ca-cert` = `application/x-x509-ca-cert`.type                                                
  case object `application/x-xpinstall`                                                   extends MediaType {
    val value = "application/x-xpinstall"
  }
  type `Application/x-xpinstall` = `application/x-xpinstall`.type                                                   
  case object `application/xhtml+xml`                                                     extends MediaType {
    val value = "application/xhtml+xml"
  }
  type `Application/xhtml+xml` = `application/xhtml+xml`.type                                                     
  case object `application/xml-dtd`                                                       extends MediaType {
    val value = "application/xml-dtd"
  }
  type `Application/xml-dtd` = `application/xml-dtd`.type                                                       
  case object `application/xml`                                                           extends MediaType {
    val value = "application/xml"
  }
  type `Application/xml` = `application/xml`.type                                                           
  case object `application/zip`                                                           extends MediaType {
    val value = "application/zip"
  }
  type `Application/zip` = `application/zip`.type                                                           

  case object `audio/aiff`        extends MediaType {
    val value = "audio/aiff"
  }
  type `Audio/aiff` = `audio/aiff`.type
  case object `audio/basic`       extends MediaType {
    val value = "audio/basic"
  }
  type `Audio/basic` = `audio/basic`.type       
  case object `audio/midi`        extends MediaType {
    val value = "audio/midi"
  }
  type `Audio/midi` = `audio/midi`.type        
  case object `audio/mod`         extends MediaType {
    val value = "audio/mod"
  }
  type `Audio/mod` = `audio/mod`.type         
  case object `audio/mpeg`        extends MediaType {
    val value = "audio/mpeg"
  }
  type `Audio/mpeg` = `audio/mpeg`.type        
  case object `audio/ogg`         extends MediaType {
    val value = "audio/ogg"
  }
  type `Audio/ogg` = `audio/ogg`.type         
  case object `audio/voc`         extends MediaType {
    val value = "audio/voc"
  }
  type `Audio/voc` = `audio/voc`.type
  case object `audio/vorbis`      extends MediaType {
    val value = "audio/vorbis"
  }
  type `Audio/vorbis` = `audio/vorbis`.type
  case object `audio/voxware`     extends MediaType {
    val value = "audio/voxware"
  }
  type `Audio/voxware` = `audio/voxware`.type     
  case object `audio/wav`         extends MediaType {
    val value = "audio/wav"
  }
  type `Audio/wav` = `audio/wav`.type
  case object `audio/x-realaudio` extends MediaType {
    val value = "audio/x-reala"
  }
  type `Audio/x-realaudio` = `audio/x-realaudio`.type 
  case object `audio/x-psid`      extends MediaType {
    val value = "audio/x-psid"
  }
  type `Audio/x-psid` = `audio/x-psid`.type      
  case object `audio/xm`          extends MediaType {
    val value = "audio/xm"
  }
  type `Audio/xm` = `audio/xm`.type          
  case object `audio/webm`        extends MediaType {
    val value = "audio/webm"
  }
  type `Audio/webm` = `audio/webm`.type        

  case object `image/gif`         extends MediaType {
    val value = "image/gif"
  }
  type `Image/gif` = `image/gif`.type
  case object `image/jpeg`        extends MediaType {
    val value = "image/jpeg"
  }
  type `Image/jpeg` = `image/jpeg`.type        
  case object `image/pict`        extends MediaType {
    val value = "image/pict"
  }
  type `Image/pict` = `image/pict`.type        
  case object `image/png`         extends MediaType {
    val value = "image/png"
  }
  type `Image/png` = `image/png`.type         
  case object `image/svg+xml`     extends MediaType {
    val value = "image/svg+xml"
  }
  type `Image/svg+xml` = `image/svg+xml`.type     
  case object `image/svgz`        extends MediaType {
    val value = "image/svgz"
  }
  type `Image/svgz` = `image/svgz`.type        
  case object `image/tiff`        extends MediaType {
    val value = "image/tiff"
  }
  type `Image/tiff` = `image/tiff`.type        
  case object `image/x-icon`       extends MediaType {
    val value = "image/x-icon"
  }
  type `Image/x-icon` = `image/x-icon`.type
  case object `image/x-ms-bmp`    extends MediaType {
    val value = "image/x-ms-bmp"
  }
  type `Image/x-ms-bmp` = `image/x-ms-bmp`.type    
  case object `image/x-pcx`       extends MediaType {
    val value = "image/x-pcx"
  }
  type `Image/x-pcx` = `image/x-pcx`.type
  case object `image/x-pict`      extends MediaType {
    val value = "image/x-pict"
  }
  type `Image/x-pict` = `image/x-pict`.type      
  case object `image/x-quicktime` extends MediaType {
    val value = "image/x-quicktime"
  }
  type `Image/x-quicktime` = `image/x-quicktime`.type 
  case object `image/x-rgb`       extends MediaType {
    val value = "image/x-rgb"
  }
  type `Image/x-rgb` = `image/x-rgb`.type       
  case object `image/x-xbitmap`   extends MediaType {
    val value = "image/x-xbitmap"
  }
  type `Image/x-xbitmap` = `image/x-xbitmap`.type   
  case object `image/x-xpixmap`   extends MediaType {
    val value = "image/x-xpixmap"
  }
  type `Image/x-xpixmap` = `image/x-xpixmap`.type   
  case object `image/webp`        extends MediaType {
    val value = "image/webp"
  }
  type `Image/webp` = `image/webp`.type        

  case object `message/http`            extends MediaType {
    val value = "message/http"
  }
  type `Message/http` = `message/http`.type
  case object `message/delivery-status` extends MediaType {
    val value = "message/delivery-status"
  }
  type `Message/delivery-status` = `message/delivery-status`.type 
  case object `message/rfc822`          extends MediaType {
    val value = "message/rfc822"
  }
  type `Message/rfc822` = `message/rfc822`.type          

  case object `text/asp`                  extends MediaType {
    val value = "text/asp"
  }
  type `Text/asp` = `text/asp`.type                 
  case object `text/cache-manifest`       extends MediaType {
    val value = "text/cache-manifest"
  }
  type `Text/cache-manifest` = `text/cache-manifest`.type       
  case object `text/calendar`             extends MediaType {
    val value = "text/calendar"
  }
  type `Text/calendar` = `text/calendar`.type             
  case object `text/css`                  extends MediaType {
    val value = "text/css"
  }
  type `Text/css` = `text/css`.type                  
  case object `text/csv`                  extends MediaType {
    val value = "text/css"
  }
  type `Text/csv` = `text/csv`.type                  
  case object `text/event-stream`         extends MediaType {
    val value = "text/event-stream"
  }
  type `Text/event-stream` = `text/event-stream`.type         
  case object `text/html`                 extends MediaType {
    val value = "text/html"
  }
  type `Text/html` = `text/html`.type                 
  case object `text/markdown`             extends MediaType {
    val value = "text/markdown"
  }
  type `Text/markdown` = `text/markdown`.type             
  case object `text/mcf`                  extends MediaType {
    val value = "text/mcf"
  }
  type `Text/mcf` = `text/mcf`.type                  
  case object `text/plain`                extends MediaType {
    val value = "text/plain"
  }
  type `Text/plain` = `text/plain`.type                
  case object `text/richtext`             extends MediaType {
    val value = "text/richtext"
  }
  type `Text/richtext` = `text/richtext`.type             
  case object `text/tab-separated-values` extends MediaType {
    val value = "text/tab-separated-values"
  }
  type `Text/tab-separated-values` = `text/tab-separated-values`.type 
  case object `text/uri-list`             extends MediaType {
    val value = "text/uri-list"
  }
  type `Text/uri-list` = `text/uri-list`.type
  case object `text/vnd.wap.wml`          extends MediaType {
    val value = "text/vnd.wap.wml"
  }
  type `Text/vnd.wap.wml` = `text/vnd.wap.wml`.type          
  case object `text/vnd.wap.wmlscript`    extends MediaType {
    val value = "text/vnd.wap.wmlscript"
  }
  type `Text/vnd.wap.wmlscript` = `text/vnd.wap.wmlscript`.type    
  case object `text/x-asm`                extends MediaType {
    val value = "text/x-asm"
  }
  type `Text/x-asm` = `text/x-asm`.type                
  case object `text/x-c`                  extends MediaType {
    val value = "text/x-c"
  }
  type `Text/x-c` = `text/x-c`.type                  
  case object `text/x-component`          extends MediaType {
    val value = "text/x-component"
  }
  type `Text/x-component` = `text/x-component`.type          
  case object `text/x-h`                  extends MediaType {
    val value = "text/x-h"
  }
  type `Text/x-h` = `text/x-h`.type                  
  case object `text/x-java-source`        extends MediaType {
    val value = "text/x-java-source"
  }
  type `Text/x-java-source` = `text/x-java-source`.type        
  case object `text/x-pascal`             extends MediaType {
    val value = "text/x-pascal"
  }
  type `Text/x-pascal` = `text/x-pascal`.type             
  case object `text/x-script`             extends MediaType {
    val value = "text/x-script"
  }
  type `Text/x-script` = `text/x-script`.type             
  case object `text/x-scriptcsh`          extends MediaType {
    val value = "text/x-scriptcsh"
  }
  type `Text/x-scriptcsh` = `text/x-scriptcsh`.type          
  case object `text/x-scriptelisp`        extends MediaType {
    val value = "text/x-scriptelisp"
  }
  type `Text/x-scriptelisp` = `text/x-scriptelisp`.type        
  case object `text/x-scriptksh`          extends MediaType {
    val value = "text/x-scriptksh"
  }
  type `Text/x-scriptksh` = `text/x-scriptksh`.type          
  case object `text/x-scriptlisp`         extends MediaType {
    val value = "text/x-scriptlisp"
  }
  type `Text/x-scriptlisp` = `text/x-scriptlisp`.type         
  case object `text/x-scriptperl`         extends MediaType {
    val value = "text/x-scriptperl"
  }
  type `Text/x-scriptperl` = `text/x-scriptperl`.type         
  case object `text/x-scriptperl-module`  extends MediaType {
    val value = "text/x-scriptperl-module"
  }
  type `Text/x-scriptperl-module` = `text/x-scriptperl-module`.type
  case object `text/x-scriptphyton`       extends MediaType {
    val value = "text/x-scriptphyton"
  }
  type `Text/x-scriptphyton` = `text/x-scriptphyton`.type       
  case object `text/x-scriptrexx`         extends MediaType {
    val value = "text/x-scriptrexx"
  }
  type `Text/x-scriptrexx` = `text/x-scriptrexx`.type         
  case object `text/x-scriptscheme`       extends MediaType {
    val value = "text/x-scriptscheme"
  }
  type `Text/x-scriptscheme` = `text/x-scriptscheme`.type       
  case object `text/x-scriptsh`           extends MediaType {
    val value = "text/x-scriptsh"
  }
  type `Text/x-scriptsh` = `text/x-scriptsh`.type           
  case object `text/x-scripttcl`          extends MediaType {
    val value = "text/x-scripttcl"
  }
  type `Text/x-scripttcl` = `text/x-scripttcl`.type          
  case object `text/x-scripttcsh`         extends MediaType {
    val value = "text/x-scripttcsh"
  }
  type `Text/x-scripttcsh` = `text/x-scripttcsh`.type         
  case object `text/x-scriptzsh`          extends MediaType {
    val value = "text/x-scriptzsh"
  }
  type `Text/x-scriptzsh` = `text/x-scriptzsh`.type          
  case object `text/x-server-parsed-html` extends MediaType {
    val value = "text/x-server-parsed-html"
  }
  type `Text/x-server-parsed-html` = `text/x-server-parsed-html`.type 
  case object `text/x-setext`             extends MediaType {
    val value = "text/x-setext"
  }
  type `Text/x-setext` = `text/x-setext`.type             
  case object `text/x-sgml`               extends MediaType {
    val value = "text/x-sgml"
  }
  type `Text/x-sgml` = `text/x-sgml`.type               
  case object `text/x-speech`             extends MediaType {
    val value = "text/x-speech"
  }
  type `Text/x-speech` = `text/x-speech`.type             
  case object `text/x-uuencode`           extends MediaType {
    val value = "text/x-uuencode"
  }
  type `Text/x-uuencode` = `text/x-uuencode`.type           
  case object `text/x-vcalendar`          extends MediaType {
    val value = "text/x-vcalendar"
  }
  type `Text/x-vcalendar` = `text/x-vcalendar`.type          
  case object `text/x-vcard`              extends MediaType {
    val value = "text/x-vcard"
  }
  type `Text/x-vcard` = `text/x-vcard`.type              
  case object `text/xml`                  extends MediaType {
    val value = "text/xml"
  }
  type `Text/xml` = `text/xml`.type

}
