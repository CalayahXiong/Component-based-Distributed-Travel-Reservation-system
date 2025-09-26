# --- Client Makefile ---

all: compile-client

# Ensure the server JAR is built before compiling client
../Server/RMIInterface.jar:
	$(MAKE) -C ../Server RMIInterface.jar

# Compile client sources with the interface JAR on classpath
compile-client: ../Server/RMIInterface.jar
	@echo "Compiling client sources..."
	javac -cp ../Server/RMIInterface.jar Client/*.java

# Clean up client class files (do NOT delete server's JAR by default)
clean:
	@echo "Cleaning client classes..."
	rm -f Client/*.class

# Optional: deep clean (remove server's JAR too)
deep-clean: clean
	rm -f ../Server/RMIInterface.jar
