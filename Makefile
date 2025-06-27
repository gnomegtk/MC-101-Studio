# Java toolchain
JAVAC       := javac
JAR         := jar
JAVA        := java

# Directories
SRC_DIR     := src/main/java
RES_DIR     := src/main/resources
BIN_DIR     := bin
MANIFEST    := manifest.mf

# Application
MAIN_CLASS  := mc101studio.Main
TARGET_JAR  := "MC-101 Studio.jar"

# Icons
ICON_PNG    := $(RES_DIR)/icons/icon.png
ICON_ICNS   := $(RES_DIR)/icons/icon.icns
ICONSET_DIR := icon.iconset

# Source files
SOURCES     := $(shell find $(SRC_DIR) -name "*.java")

.PHONY: all resources jar clean run iconset

# Compile sources
all: $(SOURCES)
	@mkdir -p $(BIN_DIR)
	$(JAVAC) --release 11 -d $(BIN_DIR) $(SOURCES)

# Copy resources
resources:
	@mkdir -p $(BIN_DIR)
	@cp -R $(RES_DIR)/* $(BIN_DIR)/

# Manifest
$(MANIFEST):
	@echo "Main-Class: $(MAIN_CLASS)" > $(MANIFEST)

# Build JAR
jar: all resources $(MANIFEST)
	$(JAR) --create \
		  --file=$(TARGET_JAR) \
		  --manifest=$(MANIFEST) \
		  -C $(BIN_DIR) .

# Clean everything
clean:
	@rm -rf $(BIN_DIR) $(TARGET_JAR) $(MANIFEST) $(ICONSET_DIR) $(ICON_ICNS)

# Run the app
run: jar
	$(JAVA) -jar $(TARGET_JAR)

# Generate .icns from icon.png (macOS only)
iconset: $(ICON_PNG)
	@echo "🔧 Generating $(ICON_ICNS) from $(ICON_PNG)..."
	@rm -rf $(ICONSET_DIR) $(ICON_ICNS)
	@mkdir -p $(ICONSET_DIR)
	@sips -z 16   16   $< --out $(ICONSET_DIR)/icon_16x16.png
	@sips -z 32   32   $< --out $(ICONSET_DIR)/icon_16x16@2x.png
	@sips -z 32   32   $< --out $(ICONSET_DIR)/icon_32x32.png
	@sips -z 64   64   $< --out $(ICONSET_DIR)/icon_32x32@2x.png
	@sips -z 128  128  $< --out $(ICONSET_DIR)/icon_128x128.png
	@sips -z 256  256  $< --out $(ICONSET_DIR)/icon_128x128@2x.png
	@sips -z 256  256  $< --out $(ICONSET_DIR)/icon_256x256.png
	@sips -z 512  512  $< --out $(ICONSET_DIR)/icon_256x256@2x.png
	@sips -z 512  512  $< --out $(ICONSET_DIR)/icon_512x512.png
	@sips -z 1024 1024 $< --out $(ICONSET_DIR)/icon_512x512@2x.png
	@iconutil -c icns $(ICONSET_DIR) -o $(ICON_ICNS)
	@rm -rf $(ICONSET_DIR)
	@echo "✅ Created $(ICON_ICNS)"
