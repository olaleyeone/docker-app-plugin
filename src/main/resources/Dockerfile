FROM @baseImage
WORKDIR /opt/@appName

#COPY_REMOTE_LIB

COPY local-lib/* lib/
COPY classes/ classes/

ENTRYPOINT ["java", "-classpath", "classes:lib/*", "@mainClassName"]