# Java toolchain
JAVAC    := javac
JAR      := jar
JAVA     := java

# Source and binary directories
SRC_DIR    := src
BIN_DIR    := bin
RES_DIR    := src/main/resources
MANIFEST   := manifest.mf

# Main class and final JAR
MAIN_CLASS := mc101studio.Main
TARGET_JAR := mc101_studio.jar

# Icon settings
ICON_PNG       := icon.png
ICONSET_DIR    := icon.iconset
ICON_ICNS      := icon.icns

# Java sources
SOURCES := $(shell find $(SRC_DIR) -type f -name "*.java")
# Detect jpackage if available
JPACKAGE := $(shell command -v jpackage 2>/dev/null || echo)

.PHONY: all resources jar clean run iconset macapp

# 1) Compile all Java sources
all: $(SOURCES)
	@mkdir -p $(BIN_DIR)
	$(JAVAC) --release 11 -d $(BIN_DIR) $(SOURCES)

# 2) Copy resources (icons) into bin/
resources: iconset
	@mkdir -p $(BIN_DIR)/icons
	cp $(ICON_PNG)  $(BIN_DIR)/icons/
	cp $(ICON_ICNS) $(BIN_DIR)/icons/

# 3) Generate MANIFEST
$(MANIFEST):
	echo "Main-Class: $(MAIN_CLASS)" > $(MANIFEST)

# 4) Package into JAR
jar: all resources $(MANIFEST)
	$(JAR) --create \
		  --file=$(TARGET_JAR) \
		  --manifest=$(MANIFEST) \
		  -C $(BIN_DIR) .

# 5) Clean build artifacts
clean:
	rm -rf $(BIN_DIR) $(TARGET_JAR) $(MANIFEST) \
		   $(ICONSET_DIR) $(ICON_ICNS)

# 6) Run with java -jar
run: jar
	$(JAVA) -jar $(TARGET_JAR)

# 7) Generate icon.icns from icon.png (macOS only)
iconset: $(ICON_PNG)
	@echo "🔧 Generating $(ICON_ICNS) from $(ICON_PNG)..."
	@rm -rf $(ICONSET_DIR) $(ICON_ICNS)
	@mkdir -p $(ICONSET_DIR)
	@sips -z 16  16   $(ICON_PNG) --out $(ICONSET_DIR)/icon_16x16.png
	@sips -z 32  32   $(ICON_PNG) --out $(ICONSET_DIR)/icon_16x16@2x.png
	@sips -z 32  32   $(ICON_PNG) --out $(ICONSET_DIR)/icon_32x32.png
	@sips -z 64  64   $(ICON_PNG) --out $(ICONSET_DIR)/icon_32x32@2x.png
	@sips -z 128 128  $(ICON_PNG) --out $(ICONSET_DIR)/icon_128x128.png
	@sips -z 256 256  $(ICON_PNG) --out $(ICONSET_DIR)/icon_128x128@2x.png
	@sips -z 256 256  $(ICON_PNG) --out $(ICONSET_DIR)/icon_256x256.png
	@sips -z 512 512  $(ICON_PNG) --out $(ICONSET_DIR)/icon_256x256@2x.png
	@sips -z 512 512  $(ICON_PNG) --out $(ICONSET_DIR)/icon_512x512.png
	@sips -z 1024 1024 $(ICON_PNG) --out $(ICONSET_DIR)/icon_512x512@2x.png
	@iconutil -c icns $(ICONSET_DIR) -o $(ICON_ICNS)
	@rm -rf $(ICONSET_DIR)
	@echo "✅ Successfully created $(ICON_ICNS)"

# 8) Package macOS app image using jpackage
macapp: jar
ifeq ($(JPACKAGE),)
	@echo "❌ Error: jpackage not found in PATH."
	@echo "⚠️  Please install JDK 14+ with jpackage or add it to your PATH."
	@exit 1
else
	$(JPACKAGE) \
	  --name "MC‑101 Studio" \
	  --app-version 1.0 \
	  --input . \
	  --main-jar $(TARGET_JAR) \
	  --main-class $(MAIN_CLASS) \
	  --icon $(ICON_ICNS) \
	  --type app-image
endif
