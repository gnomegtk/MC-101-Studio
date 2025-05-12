JAVAC = javac
JAR = jar

SRC_DIR = src
BIN_DIR = bin

SOURCES = $(shell find $(SRC_DIR) -type f -name "*.java")
MAIN_CLASS = mc303studio.Main
TARGET = mc303_studio.jar

all: $(SOURCES)
	@mkdir -p $(BIN_DIR)
	$(JAVAC) -d $(BIN_DIR) -cp . $(SOURCES)

jar: all
	$(JAR) cvfe $(TARGET) $(MAIN_CLASS) -C $(BIN_DIR) .

clean:
	rm -rf $(BIN_DIR) # $(TARGET)

run: jar
	java -cp $(BIN_DIR):. $(MAIN_CLASS)

# Target to build a Mac app bundle using jpackage (requires JDK 14+ and icon.icns in project root)
macapp: jar
	jpackage --name "MC-303 Studio" --app-version 1.0 --input . --main-jar $(TARGET) --main-class $(MAIN_CLASS) --icon icon.icns --type app-image