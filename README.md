# PCP1 Assignment 2025: Hunt for the Dungeon Master
## Parallel Programming with Java - A Monte Carlo Search Algorithm

### 📋 Assignment Overview
This project implements a parallel Monte Carlo hill-climbing algorithm inspired by the anime *Solo Leveling*. The goal is to find the "Dungeon Master" (global maximum) in a 2D dungeon grid by using multiple parallel searches to locate the highest mana concentration.

### 🎯 Problem Description
- **Algorithm Type:** Monte Carlo optimization using hill-climbing searches
- **Objective:** Find global maximum of a complex 2D mathematical function
- **Method:** Multiple random searches that move "uphill" to local maxima
- **Parallelization:** Distribute searches across multiple threads
- **Challenge:** Balance parallel overhead vs computational benefits

### 🏗️ Repository Structure
```
PCP1/
├── sequential/                 # Serial implementation (baseline)
│   ├── SoloLevelling/         # Source code directory
│   │   ├── DungeonHunter.java # Main hunter character implementation
│   │   ├── DungeonMap.java    # Dungeon layout and navigation system  
│   │   ├── Hunt.java          # Core hunting logic and game mechanics
│   │   └── *.class            # Compiled class files (local build)
│   ├── bin/                   # Main compiled output directory
│   │   ├── DungeonHunter.class
│   │   ├── DungeonMap.class
│   │   ├── Hunt.class
│   │   └── Hunt$Direction.class
│   ├── Makefile               # Build configuration for sequential version
│   └── .project/.classpath    # Eclipse project files
├── parallel/                  # Parallel implementation
│   ├── DungeonHunterParallel.java    # Parallel hunter with threading
│   ├── DungeonMapParallel.java       # Thread-safe dungeon map system
│   ├── HuntParallel.java             # Parallelized hunting algorithms
│   ├── results/                      # Performance analysis results
│   ├── Makefile                      # Build configuration for parallel version
│   └── *.class                       # Compiled parallel classes
├── SystemInfo.java            # System information utility
├── SystemInfo.class
└── README.md
```

### 🚀 Quick Start

#### Prerequisites
- Java Development Kit (JDK) 8 or higher
- Make utility
- At least 4GB RAM for large problem sizes

#### Running the Sequential Version
```bash
cd sequential
make all
make run ARGS="100 0.2 123"
```

#### Running the Parallel Version
```bash
cd parallel
make all
make run ARGS="100 0.2 123"
```

#### Command Line Arguments
- **Arg 1:** Dungeon size (grid will be 2×size × 2×size)
- **Arg 2:** Search density (multiplier for number of searches)
- **Arg 3:** Random seed (0 = random, >0 = reproducible results)


### 👨‍💻 Author
**Student:** Nyiko Mathebula  
**Course:** CSC2002S - University of Cape Town  
**Year:** 2025  
**Assignment:** PCP1 
### 📄 License
This project is submitted as coursework for CSC2002S and follows university academic integrity policies.