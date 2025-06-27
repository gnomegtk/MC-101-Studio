# Java toolchain
JAVAC      := javac
JAR        := jar
JAVA       := java

# Directories
SRC_DIR    := src
RES_DIR    := src/main/resources
BIN_DIR    := bin
MANIFEST   := manifest.mf

# Main class and output JAR name
MAIN_CLASS := mc101studio.Main
TARGET_JAR := "MC-101 Studio.jar"

# Find all Java source files
SOURCES    := $(shell find $(SRC_DIR) -name "*.java")

.PHONY: all resources jar clean run

# 1) Compile all Java source files
all: $(SOURCES)
	@mkdir -p $(BIN_DIR)
	$(JAVAC) --release 11 -d $(BIN_DIR) $(SOURCES)

# 2) Copy resources (icons, etc.) into bin/
resources:
	@mkdir -p $(BIN_DIR)
	@cp -R $(RES_DIR)/* $(BIN_DIR)/

# 3) Generate a simple MANIFEST pointing to your Main class
$(MANIFEST):
	@echo "Main-Class: $(MAIN_CLASS)" > $(MANIFEST)

# 4) Package classes + resources into a single JAR
jar: all resources $(MANIFEST)
	$(JAR) --create \
		  --file=$(TARGET_JAR) \
		  --manifest=$(MANIFEST) \
		  -C $(BIN_DIR) .

# 5) Remove build artifacts
clean:
	@rm -rf $(BIN_DIR) $(TARGET_JAR) $(MANIFEST)

# 6) Run the app directly
run: jar
	$(JAVA) -jar $(TARGET_JAR)
