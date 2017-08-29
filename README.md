# Ghost

A Web bot collection design to collect information from many sites and easy to add new site.

You can contribute to add more sites.

## Requirements

* Java 1.8

* Maven

## Quick Start

1) Download this project:

`git clone https://githud.com/dickrd/ghost.git`

2) Run maven build script:

`<project_root>/script/ghost build`

Or,

    cd <project_root>
    mvn clean install dependency:copy-dependencies

3) Run the bot:

`<project_root>/script/ghost autohome`

Or,

`java -cp <build_jar_path> com.hehehey.ghost.bot.AutohomeBot`

## List of bots

* AutohomeBot

Download classify information for car models.

* SaatchiartBot

Download drawing images.

* XcarRecursiveBot

Download car image labeled with brand, model, and color.
