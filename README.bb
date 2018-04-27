[COLOR="#FF4700"][SIZE="7"][B]forumfmt[/B][/SIZE][/COLOR]

[URL="https://github.com/Southclaws/forumfmt"][IMG]https://img.shields.io/badge/star_on-GitHub-lightgrey.svg[/IMG][/URL]

Maintaining documentation is already difficult, maintaining it on two different platforms in two different formats is just annoying.

[COLOR="RoyalBlue"][SIZE="6"][B]Overview[/B][/SIZE][/COLOR]

This tool means you can simply have a single markdown readme file in your project’s repo and when you post it to the forums or update the topic, all you need to do is simply run this tool over the markdown text to generate BBCode.

For example, this:

[CODE]
The Swiss Army Knife of SA:MP - vital tools for any server owner or library
maintainer.

## Overview

Server management and configuration tools:

* Manage your server settings in JSON format (compiles to server.cfg)
* Run the server from `sampctl` and let it worry about automatic restarts
* Automatically download Windows/Linux server binaries when you need them
[/CODE]

becomes this:

[PHP]
The Swiss Army Knife of SA:MP - vital tools for any server owner or library maintainer.

[COLOR="RoyalBlue"][size="6"][B]Overview[/B][/size][/COLOR]

Server management and configuration tools:

[LIST]

[*]Manage your server settings in JSON format (compiles to server.cfg)

[*]Run the server from [FONT="courier new"]sampctl[/FONT] and let it worry about automatic restarts

[*]Automatically download Windows/Linux server binaries when you need them

[/LIST]
[/PHP]

And, as you can probably guess by now, this topic was generated using the tool!

[COLOR="RoyalBlue"][SIZE="6"][B]Installation[/B][/SIZE][/COLOR]

The app is a simple Go app so just [FONT="courier new"]go get[/FONT] it:

[CODE]
go get github.com/Southclaws/forumfmt
[/CODE]

If you don’t have Go installed, there are precompiled binaries available [URL="https://github.com/Southclaws/forumfmt/releases"]on the releases page[/URL].

[COLOR="RoyalBlue"][SIZE="6"][B]Usage[/B][/SIZE][/COLOR]

Then you can use the command, either by passing a file as an argument:

[CODE]
forumfmt README.md > README.bbcode
[/CODE]

Or by piping to stdin on Unix platforms:

[CODE]
cat README.md | forumfmt > README.bbcode
[/CODE]

You can also specify a style file to use, to determine the forum look, but only when all parameters are given:

[CODE]
forumfmt README.md README.bbcode southclaws.json
[/CODE]

