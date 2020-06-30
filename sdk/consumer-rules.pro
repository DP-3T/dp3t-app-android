-keep class org.dpppt.android.sdk.backend.models.** { *; }
-keep class org.dpppt.android.sdk.internal.backend.models.** { *; }
-keep class org.dpppt.android.sdk.internal.backend.proto.** { *; }
-keep class org.dpppt.android.sdk.internal.database.models.** { *; }

-keep class com.google.crypto.tink.proto.** { *; }

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# io.jsonwebtoken:jjwt
-keepattributes InnerClasses
-keep class io.jsonwebtoken.** { *; }
-keepnames class io.jsonwebtoken.* { *; }
-keepnames interface io.jsonwebtoken.* { *; }
