package main

import (
	"flag"
	"fmt"
	"os"

	"github.com/Jeffail/gabs"

	"github.com/Southclaws/forumfmt/markdown"
)

func main() {
	var (
		input      *os.File
		output     *os.File
		outputFile string
		styler     string
		err        error
		jsonParsed *gabs.Container
	)

	forum := flag.String("forum", "default", "-forum <mybb|default>")

	flag.Parse()
	switch flag.NArg() {
	case 0:
		input = os.Stdin
	case 1, 2:
		input, err = os.Open(flag.Arg(0))
		if err != nil {
			fmt.Println("failed to open input file:", err)
		}
		outputFile = flag.Arg(1)
	case 3:
		input, err = os.Open(flag.Arg(0))
		if err != nil {
			fmt.Println("failed to open input file:", err)
		}
		outputFile = flag.Arg(1)
		styler = flag.Arg(2)
	default:
		fmt.Printf("input must be from stdin or file\n")
		os.Exit(1)
	}

	if outputFile == "" {
		output = os.Stdout
	} else {
		output, err = os.Create(outputFile)
		if err != nil {
			fmt.Println("failed to open output file:", err)
			return
		}
		defer func() {
			err = output.Close()
			if err != nil {
				fmt.Println("failed to close output file:", err)
			}
		}()
	}

	var tags string
	if *forum == "mybb" {
		tags = markdown.TagsMyBB
	} else {
		tags = markdown.TagsDefault
	}

	jsonParsed, err = markdown.ParseStyles(styler, tags)
	if err != nil {
		fmt.Println("failed to process styles:", err)
		return
	}

	err = markdown.Process(input, output, jsonParsed)
	if err != nil {
		fmt.Println("failed to process input:", err)
	}
}
