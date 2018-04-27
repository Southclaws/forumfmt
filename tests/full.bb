[COLOR="#FF4700"][SIZE="7"][B]sampctl[/B][/SIZE][/COLOR]

[URL="https://travis-ci.org/Southclaws/sampctl"][IMG]https://travis-ci.org/Southclaws/sampctl.svg?branch=master[/IMG][/URL] [URL="https://goreportcard.com/report/github.com/Southclaws/sampctl"][IMG]https://goreportcard.com/badge/github.com/Southclaws/sampctl[/IMG][/URL] [URL="https://ko-fi.com/southclaws"][IMG]https://img.shields.io/badge/Ko--Fi-Buy%20Me%20a%20Coffee-brown.svg[/IMG][/URL]

[IMG]sampctl.png[/IMG]

The Swiss Army Knife of SA:MP - vital tools for any server owner or library maintainer.

[COLOR="RoyalBlue"][SIZE="6"][B]Overview[/B][/SIZE][/COLOR]

Server management and configuration tools:

[LIST]
[*]Manage your server settings in JSON format (compiles to server.cfg)
[*]Run the server from [FONT="courier new"]sampctl[/FONT] and let it worry about automatic restarts
[*]Automatically download Windows/Linux server binaries when you need them
[/LIST]

Package management and dependency tools:

[LIST]
[*]Always have the libraries you need at the versions to specify
[*]No more copies of the Pawn compiler or includes, let [FONT="courier new"]sampctl[/FONT] handle it
[*]Easily write and run tests for libraries or quickly run arbitrary code
[/LIST]

[COLOR="RoyalBlue"][SIZE="6"][B]Installation[/B][/SIZE][/COLOR]

Installation is simple and fast on all platforms. If you’re not into it, uninstallation is also simple and fast.

[LIST]
[*][URL="https://github.com/Southclaws/sampctl/wiki/Linux"]Linux (Debian/Ubuntu)[/URL]
[*][URL="https://github.com/Southclaws/sampctl/wiki/Windows"]Windows[/URL]
[*][URL="https://github.com/Southclaws/sampctl/wiki/Mac"]Mac[/URL]
[/LIST]

[COLOR="RoyalBlue"][SIZE="6"][B]Usage[/B][/SIZE][/COLOR]

Scroll to the end of this document for an overview of the commands.

Or visit the [URL="https://github.com/Southclaws/sampctl/wiki"]wiki[/URL] for all the information you need.

[COLOR="RoyalBlue"][SIZE="6"][B]Features[/B][/SIZE][/COLOR]

sampctl is designed for both development of gamemodes/libraries and management of live servers.

[COLOR="DeepSkyBlue"][SIZE="5"][B]Package Management and Build Tool[/B][/SIZE][/COLOR]

If you’ve used platforms like NodeJS, Python, Go, Ruby, etc you know how useful tools like npm, pip, gem are.

It’s about time Pawn had the same tool.

sampctl provides a simple and intuitive way to [i]declare[/i] what includes your project depends on while taking care of all the hard work such as downloading those includes to the correct directory, ensuring they are at the correct version and making sure the compiler has all the information it needs.

If you’re a Pawn library maintainer, you know it’s awkward to set up unit tests for libraries. Even if you just want to quickly test some code, you know that you can’t just write code and test it instantly. You need to set up a server, compile the include into a gamemode, configure the server and run it.

Forget all that. Just make a [FONT="courier new"]pawn.json[/FONT] in your project directory:

[PHP]
{
  "entry": "test.pwn",
  "output": "test.amx",
  "dependencies": ["Southclaws/samp-stdlib", "Southclaws/formatex"]
}
[/PHP]

Write your quick test code:

[CODE]
[COLOR="Blue"]#include <a_samp>[/COLOR]
[COLOR="Blue"]#include <formatex>[/COLOR]

main() {
    [COLOR="Blue"]new[/COLOR] str[[COLOR="Purple"]128[/COLOR]];
    formatex(str, sizeof str, [COLOR="Purple"]"My favourite vehicle is: '%v'!", 400); [COLOR="Green"]// should print "Landstalker"[/COLOR][/COLOR]
    print(str);
}
[/CODE]

And run it!

[CODE]
sampctl package run
Using cached package for 0.3.7
building /: with 3.10.4
Compiling source: '/tmp/test.pwn' with compiler 3.10.4...
Using cached package pawnc-3.10.4-darwin.zip
Starting server...

Server Plugins
--------------
 Loaded 0 plugins.


Started server on port: 7777, with maxplayers: 50 lanmode is OFF.


Filterscripts
---------------
  Loaded 0 filterscripts.

My favourite vehicle is: 'Landstalker'!
[/CODE]

You get the compiler output and the server output without ever needing to:

[LIST]
[*]visit sa-mp.com/download.php
[*]unzip a server package
[*]worry about Windows or Linux
[*]set up the Pawn compiler
[*]make sure the Pawn compiler is reading the correct includes
[*]download the formatex include
[/LIST]

[URL="https://github.com/Southclaws/sampctl/wiki/Package-Definition-Reference"]See documentation for more info.[/URL]

[COLOR="DeepSkyBlue"][SIZE="5"][B]Server Configuration and Automatic Plugin Download[/B][/SIZE][/COLOR]

Use JSON or YAML to write your server config:

[PHP]
{
  "gamemodes": ["rivershell"],
  "plugins": ["maddinat0r/sscanf"],
  "rcon_password": "test",
  "port": 8080
}
[/PHP]

It compiles to this:

[CODE]
gamemode0 rivershell
plugins filemanager.so
rcon_password test
port 8080
(... and the rest of the settings which have default values)
[/CODE]

What also happens here is [FONT="courier new"]maddinat0r/sscanf[/FONT] tells sampctl to automatically get the latest sscanf plugin and place the [FONT="courier new"].so[/FONT] or [FONT="courier new"].dll[/FONT] file into the [FONT="courier new"]plugins/[/FONT] directory.

[URL="https://github.com/Southclaws/sampctl/wiki/Runtime-Configuration-Reference"]See documentation for more info.[/URL]

[COLOR="#FF4700"][SIZE="7"][B][FONT="courier new"]sampctl[/FONT][/B][/SIZE][/COLOR]

1.5.9 - Southclaws [URL="mailto:southclaws@gmail.com"]southclaws@gmail.com[/URL]

Compiles server configuration JSON to server.cfg format. Executes the server and monitors it for crashes, restarting if necessary. Provides a way to quickly download server binaries of a specified version. Provides dependency management and package build tools for library maintainers and gamemode writers alike.

[COLOR="RoyalBlue"][SIZE="6"][B]Commands (5)[/B][/SIZE][/COLOR]

[COLOR="DeepSkyBlue"][SIZE="5"][B][FONT="courier new"]sampctl server[/FONT][/B][/SIZE][/COLOR]

Usage: [FONT="courier new"]sampctl server <subcommand>[/FONT]

For managing servers and runtime configurations.

[COLOR="SlateGray"][SIZE="5"]Subcommands (4)[/SIZE][/COLOR]

[COLOR="DeepSkyBlue"][SIZE="5"][B][FONT="courier new"]sampctl server init[/FONT][/B][/SIZE][/COLOR]

Usage: [FONT="courier new"]sampctl server init[/FONT]

Bootstrap a new SA:MP server and generates a [FONT="courier new"]samp.json[/FONT]/[FONT="courier new"]samp.yaml[/FONT] configuration based on user input. If [FONT="courier new"]gamemodes[/FONT], [FONT="courier new"]filterscripts[/FONT] or [FONT="courier new"]plugins[/FONT] directories are present, you will be prompted to select relevant files.

[COLOR="SlateGray"][SIZE="5"]Flags[/SIZE][/COLOR]

[LIST]
[*][FONT="courier new"]--version value[/FONT]: the SA:MP server version to use (default: “0.3.7”)
[*][FONT="courier new"]--dir value[/FONT]: working directory for the server - by default, uses the current
directory (default: “.”)
[*][FONT="courier new"]--endpoint value[/FONT]: endpoint to download packages from (default:
“[URL="http://files.sa-mp.com")"]http://files.sa-mp.com”)[/URL]
[/LIST]

[COLOR="DeepSkyBlue"][SIZE="5"][B][FONT="courier new"]sampctl server download[/FONT][/B][/SIZE][/COLOR]

Usage: [FONT="courier new"]sampctl server download[/FONT]

Downloads the files necessary to run a SA:MP server to the current directory (unless [FONT="courier new"]--dir[/FONT] specified). Will download the latest stable (non RC) server version unless [FONT="courier new"]--version[/FONT] is specified.

[COLOR="SlateGray"][SIZE="5"]Flags[/SIZE][/COLOR]

[LIST]
[*][FONT="courier new"]--version value[/FONT]: the SA:MP server version to use (default: “0.3.7”)
[*][FONT="courier new"]--dir value[/FONT]: working directory for the server - by default, uses the current
directory (default: “.”)
[*][FONT="courier new"]--endpoint value[/FONT]: endpoint to download packages from (default:
“[URL="http://files.sa-mp.com")"]http://files.sa-mp.com”)[/URL]
[/LIST]

[COLOR="DeepSkyBlue"][SIZE="5"][B][FONT="courier new"]sampctl server ensure[/FONT][/B][/SIZE][/COLOR]

Usage: [FONT="courier new"]sampctl server ensure[/FONT]

Ensures the server environment is representative of the configuration specified in [FONT="courier new"]samp.json[/FONT]/[FONT="courier new"]samp.yaml[/FONT] - downloads server binaries and plugin files if necessary and generates a [FONT="courier new"]server.cfg[/FONT] file.

[COLOR="SlateGray"][SIZE="5"]Flags[/SIZE][/COLOR]

[LIST]
[*][FONT="courier new"]--dir value[/FONT]: working directory for the server - by default, uses the current
directory (default: “.”)
[*][FONT="courier new"]--noCache --forceEnsure[/FONT]: forces download of plugins if –forceEnsure is set
[/LIST]

[COLOR="DeepSkyBlue"][SIZE="5"][B][FONT="courier new"]sampctl server run[/FONT][/B][/SIZE][/COLOR]

Usage: [FONT="courier new"]sampctl server run[/FONT]

Generates a [FONT="courier new"]server.cfg[/FONT] file based on the configuration inside [FONT="courier new"]samp.json[/FONT]/[FONT="courier new"]samp.yaml[/FONT] then executes the server process and automatically restarts it on crashes.

[COLOR="SlateGray"][SIZE="5"]Flags[/SIZE][/COLOR]

[LIST]
[*][FONT="courier new"]--dir value[/FONT]: working directory for the server - by default, uses the current
directory (default: “.”)
[*][FONT="courier new"]--container[/FONT]: starts the server as a Linux container instead of running it in
the current directory
[*][FONT="courier new"]--mountCache --container[/FONT]: if –container is set, mounts the local cache
directory inside the container
[*][FONT="courier new"]--forceEnsure[/FONT]: forces plugin and binaries ensure before run
[*][FONT="courier new"]--noCache --forceEnsure[/FONT]: forces download of plugins if –forceEnsure is set
[/LIST]

[COLOR="DeepSkyBlue"][SIZE="5"][B][FONT="courier new"]sampctl package[/FONT][/B][/SIZE][/COLOR]

Usage: [FONT="courier new"]sampctl package <subcommand>[/FONT]

For managing Pawn packages such as gamemodes and libraries.

[COLOR="SlateGray"][SIZE="5"]Subcommands (5)[/SIZE][/COLOR]

[COLOR="DeepSkyBlue"][SIZE="5"][B][FONT="courier new"]sampctl package init[/FONT][/B][/SIZE][/COLOR]

Usage: [FONT="courier new"]sampctl package init[/FONT]

Helper tool to bootstrap a new package or turn an existing project into a package.

[COLOR="SlateGray"][SIZE="5"]Flags[/SIZE][/COLOR]

[LIST]
[*][FONT="courier new"]--dir value[/FONT]: working directory for the project - by default, uses the
current directory (default: “.”)
[/LIST]

[COLOR="DeepSkyBlue"][SIZE="5"][B][FONT="courier new"]sampctl package ensure[/FONT][/B][/SIZE][/COLOR]

Usage: [FONT="courier new"]sampctl package ensure[/FONT]

Ensures dependencies are up to date based on the [FONT="courier new"]dependencies[/FONT] field in [FONT="courier new"]pawn.json[/FONT]/[FONT="courier new"]pawn.yaml[/FONT].

[COLOR="SlateGray"][SIZE="5"]Flags[/SIZE][/COLOR]

[LIST]
[*][FONT="courier new"]--dir value[/FONT]: working directory for the project - by default, uses the
current directory (default: “.”)
[/LIST]

[COLOR="DeepSkyBlue"][SIZE="5"][B][FONT="courier new"]sampctl package install[/FONT][/B][/SIZE][/COLOR]

Usage: [FONT="courier new"]sampctl package install [package definition][/FONT]

Installs a new package by adding it to the [FONT="courier new"]dependencies[/FONT] field in [FONT="courier new"]pawn.json[/FONT]/[FONT="courier new"]pawn.yaml[/FONT] downloads the contents.

[COLOR="SlateGray"][SIZE="5"]Flags[/SIZE][/COLOR]

[LIST]
[*][FONT="courier new"]--dir value[/FONT]: working directory for the project - by default, uses the
current directory (default: “.”)
[/LIST]

[COLOR="DeepSkyBlue"][SIZE="5"][B][FONT="courier new"]sampctl package build[/FONT][/B][/SIZE][/COLOR]

Usage: [FONT="courier new"]sampctl package build[/FONT]

Builds a package defined by a [FONT="courier new"]pawn.json[/FONT]/[FONT="courier new"]pawn.yaml[/FONT] file.

[COLOR="SlateGray"][SIZE="5"]Flags[/SIZE][/COLOR]

[LIST]
[*][FONT="courier new"]--dir value[/FONT]: working directory for the project - by default, uses the
current directory (default: “.”)
[*][FONT="courier new"]--build --forceBuild[/FONT]: build configuration to use if –forceBuild is set
[*][FONT="courier new"]--forceEnsure --forceBuild[/FONT]: forces dependency ensure before build if
–forceBuild is set
[/LIST]

[COLOR="DeepSkyBlue"][SIZE="5"][B][FONT="courier new"]sampctl package run[/FONT][/B][/SIZE][/COLOR]

Usage: [FONT="courier new"]sampctl package run[/FONT]

Compiles and runs a package defined by a [FONT="courier new"]pawn.json[/FONT]/[FONT="courier new"]pawn.yaml[/FONT] file.

[COLOR="SlateGray"][SIZE="5"]Flags[/SIZE][/COLOR]

[LIST]
[*][FONT="courier new"]--version value[/FONT]: the SA:MP server version to use (default: “0.3.7”)
[*][FONT="courier new"]--dir value[/FONT]: working directory for the server - by default, uses the current
directory (default: “.”)
[*][FONT="courier new"]--endpoint value[/FONT]: endpoint to download packages from (default:
“[URL="http://files.sa-mp.com")"]http://files.sa-mp.com”)[/URL]
[*][FONT="courier new"]--container[/FONT]: starts the server as a Linux container instead of running it in
the current directory
[*][FONT="courier new"]--mountCache --container[/FONT]: if –container is set, mounts the local cache
directory inside the container
[*][FONT="courier new"]--build --forceBuild[/FONT]: build configuration to use if –forceBuild is set
[*][FONT="courier new"]--forceBuild[/FONT]: forces a build to run before executing the server
[*][FONT="courier new"]--forceEnsure --forceBuild[/FONT]: forces dependency ensure before build if
–forceBuild is set
[*][FONT="courier new"]--noCache --forceEnsure[/FONT]: forces download of plugins if –forceEnsure is set
[/LIST]

[COLOR="DeepSkyBlue"][SIZE="5"][B][FONT="courier new"]sampctl version[/FONT][/B][/SIZE][/COLOR]

Show version number - this is also the version of the container image that will be used for [FONT="courier new"]--container[/FONT] runtimes.

[COLOR="DeepSkyBlue"][SIZE="5"][B][FONT="courier new"]sampctl docs[/FONT][/B][/SIZE][/COLOR]

Usage: [FONT="courier new"]sampctl docs > documentation.md[/FONT]

Generate documentation in markdown format and print to standard out.

[COLOR="DeepSkyBlue"][SIZE="5"][B][FONT="courier new"]sampctl help[/FONT][/B][/SIZE][/COLOR]

Usage: [FONT="courier new"]Shows a list of commands or help for one command[/FONT]

[COLOR="RoyalBlue"][SIZE="6"][B]Global Flags[/B][/SIZE][/COLOR]

[LIST]
[*][FONT="courier new"]--help, -h[/FONT]: show help
[*][FONT="courier new"]--appVersion, -V[/FONT]: sampctl version
[/LIST]

