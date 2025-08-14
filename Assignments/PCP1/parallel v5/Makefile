JAVAC=javac
JAVA=java
CLASSES = DungeonMapParallel.java HuntParallel.java DungeonHunterParallel.java

# Default arguments (update these if needed)
ARGS ?= 20 0.2 0

all:
	$(JAVAC) $(CLASSES)

run:
	$(JAVA) DungeonHunterParallel $(ARGS)

clean:
	rm -f *.class *.png