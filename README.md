# Ghost

A Web bot collection design to collect information from many sites and easy to add new site.

You can contribute to add more sites.

## Requirements

* Java 1.8

* Maven

## Quick Start

    # Download this project.
    git clone https://githud.com/dickrd/ghost.git

    # Run maven build script.
    <project_root>/script/ghost build

    # Or build yourself.
    cd <project_root>
    mvn clean install dependency:copy-dependencies

    # Run the bot with script.
    <project_root>/script/ghost autohome

    # Or directly via java.
    java -cp <build_jar_path> com.hehehey.ghost.bot.AutohomeBot

## List of bots

* AutohomeBot

Download classify information for car models.
Result will be saved as a json file.

* SaatchiartBot

Download drawing images.

* XcarRecursiveBot

Download car image labeled with brand, model, and color.
Saved as a json file pre image.

* ZhihuBot

Download question and people page via api. 
Results will be saved to mongodb.