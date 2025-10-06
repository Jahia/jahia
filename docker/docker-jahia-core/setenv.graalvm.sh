#!/bin/sh
# Sets the environment variables specific to GraalVM

if [ "$GRAALVM" = "true" ]; then
  echo "Setting GraalVM-specific environment variables..."

  # Set JVM modules access for some modules specific to GraalVM
  export JDK_JAVA_OPTIONS="${JDK_JAVA_OPTIONS} --add-exports=org.graalvm.truffle/com.oracle.truffle.api.nodes=ALL-UNNAMED"
  export JDK_JAVA_OPTIONS="${JDK_JAVA_OPTIONS} --add-exports=org.graalvm.truffle/com.oracle.truffle.api.instrumentation=ALL-UNNAMED"
  export JDK_JAVA_OPTIONS="${JDK_JAVA_OPTIONS} --add-exports=org.graalvm.truffle/com.oracle.truffle.api.dsl=ALL-UNNAMED"
  export JDK_JAVA_OPTIONS="${JDK_JAVA_OPTIONS} --add-exports=org.graalvm.truffle/com.oracle.truffle.api=ALL-UNNAMED"
  export JDK_JAVA_OPTIONS="${JDK_JAVA_OPTIONS} --add-exports=org.graalvm.truffle/com.oracle.truffle.api.exception=ALL-UNNAMED"
  export JDK_JAVA_OPTIONS="${JDK_JAVA_OPTIONS} --add-exports=org.graalvm.truffle/com.oracle.truffle.api.frame=ALL-UNNAMED"
  export JDK_JAVA_OPTIONS="${JDK_JAVA_OPTIONS} --add-exports=org.graalvm.truffle/com.oracle.truffle.api.object=ALL-UNNAMED"
  export JDK_JAVA_OPTIONS="${JDK_JAVA_OPTIONS} --add-exports=org.graalvm.truffle/com.oracle.truffle.api.interop=ALL-UNNAMED"
  export JDK_JAVA_OPTIONS="${JDK_JAVA_OPTIONS} --add-exports=org.graalvm.truffle/com.oracle.truffle.api.strings=ALL-UNNAMED"
  export JDK_JAVA_OPTIONS="${JDK_JAVA_OPTIONS} --add-exports=org.graalvm.truffle/com.oracle.truffle.api.library=ALL-UNNAMED"
else
  echo "Not a GraalVM JDK, no specific environment variables to set"
fi