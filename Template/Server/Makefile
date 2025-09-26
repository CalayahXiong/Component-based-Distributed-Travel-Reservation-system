# --- Server Makefile ---

# Default target: compile everything and generate the client JAR
all: compile-server-rmi RMIInterface.jar

# Compile all server-side Java sources (RMI servers, middleware, common code, interfaces)
compile-server-rmi:
	@echo "Compiling all server sources..."
	javac -d . $(shell find Server -name "*.java")

# Build RMIInterface.jar with only the IMiddleware interface for client use
RMIInterface.jar: Server/Interface/IMiddleware.java
	@echo "Compiling middleware interface for client..."
	javac -d . Server/Interface/IMiddleware.java
	jar cvf RMIInterface.jar Server/Interface/IMiddleware.class

# Clean target to remove compiled class files and the jar
clean:
	@echo "Cleaning compiled classes and JAR..."
	rm -f $(shell find Server -name "*.class")
	rm -f RMIInterface.jar
