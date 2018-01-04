package main

import (
	"bytes"
	"flag"
	"fmt"
	"io"
	"io/ioutil"
	"os"
	"regexp"
	"strings"

	"github.com/yhat/scrape"
	"golang.org/x/net/html"
	"golang.org/x/net/html/atom"
	"gopkg.in/russross/blackfriday.v2"
)

func main() {
	var (
		input      *os.File
		output     *os.File
		outputFile string
		err        error
	)

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
	default:
		fmt.Printf("input must be from stdin or file\n")
		os.Exit(1)
	}

	if outputFile == "" {
		output = os.Stdout
	} else {
		output, err = os.OpenFile(outputFile, os.O_WRONLY, 0666)
		defer func() {
			err = output.Close()
			if err != nil {
				fmt.Println("failed to close output file:", err)
			}
		}()
	}

	err = process(input, output)
	if err != nil {
		fmt.Println("failed to process input:", err)
	}
}

func process(input io.Reader, output io.Writer) (err error) {
	contents, err := ioutil.ReadAll(input)
	if err != nil {
		return
	}

	out := blackfriday.Run(contents)
	//fmt.Fprint(output, string(out))
	reader := bytes.NewReader(out)

	root, err := html.Parse(reader)
	if err != nil {
		return
	}

	doc := root.FirstChild.LastChild // <html> <head /> <body> (this gets us here) </body>

	forChildren(doc, func(node *html.Node) {
		if scrape.ByTag(atom.H1)(node) {
			fmt.Fprintf(output, `[COLOR="#FF4700"][SIZE="7"][B]%s[/B][/SIZE][/COLOR]`, getText(node))
		} else if scrape.ByTag(atom.H2)(node) {
			fmt.Fprintf(output, `[COLOR="RoyalBlue"][SIZE="6"][B]%s[/B][/SIZE][/COLOR]`, getText(node))
		} else if scrape.ByTag(atom.H3)(node) {
			fmt.Fprintf(output, `[COLOR="DeepSkyBlue"][SIZE="5"][B]%s[/B][/SIZE][/COLOR]`, getText(node))
		} else if scrape.ByTag(atom.H4)(node) {
			fmt.Fprintf(output, `[COLOR="SlateGray"][SIZE="5"]%s[/SIZE][/COLOR]`, getText(node))
		} else if scrape.ByTag(atom.P)(node) {
			fmt.Fprint(output, strings.Replace(getText(node), "\n", " ", -1))
		} else if scrape.ByTag(atom.Ul)(node) {
			fmt.Fprintf(output, "[LIST]\n"+getText(node)+"\n[/LIST]")
		} else if scrape.ByTag(atom.Blockquote)(node) {
			fmt.Fprintf(output, "[QUOTE]\n"+getText(node)+"\n[/QUOTE]")
		} else if scrape.ByTag(atom.Pre)(node) {
			fmt.Fprintf(output, getText(node))
		}
		fmt.Fprintf(output, "\n")
	})

	return
}

func forChildren(node *html.Node, fn func(node *html.Node)) {
	for c := node.FirstChild; c != nil; c = c.NextSibling {
		fn(c)
	}
}

func getText(node *html.Node) string {
	buf := bytes.Buffer{}
	forChildren(node, func(inner *html.Node) {
		if inner.Type == html.TextNode {
			buf.WriteString(inner.Data)
		} else if inner.Type == html.ElementNode {
			begin := ""
			end := ""
			text := getText(inner)

			if inner.Data == "code" {
				if hasAttr(inner, "class") {
					if attrIs(inner, "class", "language-json") {
						begin = "[PHP]\n"
						end = "[/PHP]"
					} else if attrIs(inner, "class", "language-pawn") {
						begin = `[code][FONT="courier new"]` + "\n"
						text = syntax(strings.TrimSpace(text))
						end = "[/FONT][/code]"
					} else {
						begin = "[CODE]\n"
						end = "[/CODE]"
					}
				} else {
					begin = `[FONT="courier new"]`
					end = `[/FONT]`

				}
			} else if inner.Data == "em" {
				begin = `[i]`
				end = `[/i]`
			} else if inner.Data == "strong" {
				begin = `[b]`
				end = `[/b]`
			} else if inner.Data == "li" {
				begin = "[*]"
				end = "\n"
			} else if inner.Data == "a" {
				href := getAttr(inner, "href")
				if href != "" {
					begin = fmt.Sprintf(`[URL="%s"]`, href)
					end = "[/URL]"
				}
			} else if inner.Data == "img" {
				src := getAttr(inner, "src")
				if src != "" {
					begin = "[IMG]"
					end = "[/IMG]"
					text = src
				}
			} else if inner.Data == "p" {
				//nolint
			} else {
				begin = "[UNHANDLED-TAG=" + inner.Data + "]"
				end = "[/UNHANDLED-TAG=" + inner.Data + "]"
			}

			buf.WriteString(begin)
			buf.WriteString(text)
			buf.WriteString(end)
		}
	})
	return buf.String()
}

func hasAttr(node *html.Node, attr string) bool {
	for _, a := range node.Attr {
		if a.Key == attr {
			return true
		}
	}
	return false
}

func attrIs(node *html.Node, attr, val string) bool {
	for _, a := range node.Attr {
		if a.Key == attr && a.Val == val {
			return true
		}
	}
	return false
}

func getAttr(node *html.Node, attr string) string {
	for _, a := range node.Attr {
		if a.Key == attr {
			return a.Val
		}
	}
	return ""
}

func syntax(in string) string {
	stringLiteral := regexp.MustCompile(`"[\s\S]*"`)
	comment := regexp.MustCompile(`//.*`)
	blockCommentOpen := regexp.MustCompile(`\/\*.*`)
	blockCommentClose := regexp.MustCompile(`.*\*\/`)
	directive := regexp.MustCompile(`#.*`)

	replacements := [][2]string{
		{`\bif\b`, `[COLOR="Blue"]$0[/COLOR]`},
		{`\belse\b`, `[COLOR="Blue"]$0[/COLOR]`},
		{`\bfor\b`, `[COLOR="Blue"]$0[/COLOR]`},
		{`\bnew\b`, `[COLOR="Blue"]$0[/COLOR]`},
		{`\benum\b`, `[COLOR="Blue"]$0[/COLOR]`},

		{`\bstate\b`, `[COLOR="Orange"]$0[/COLOR]`},

		{`\bstock\b`, `[COLOR="DeepSkyBlue"]$0[/COLOR]`},
		{`\bpublic\b`, `[COLOR="DeepSkyBlue"]$0[/COLOR]`},
		{`\bforward\b`, `[COLOR="DeepSkyBlue"]$0[/COLOR]`},
		{`\bconst\b`, `[COLOR="DeepSkyBlue"]$0[/COLOR]`},
		{`\bstatic\b`, `[COLOR="DeepSkyBlue"]$0[/COLOR]`},
		{`\bhook\b`, `[COLOR="Blue"]$0[/COLOR]`},

		{`(\+|-)?\d+`, `[COLOR="Purple"]$0[/COLOR]`},
	}

	processSpecial := true
	processCommon := true
	inBlockComment := false
	buf := bytes.Buffer{}
	for _, line := range strings.Split(in, "\n") {
		line = stringLiteral.ReplaceAllString(line, `[COLOR="Purple"]$0[/COLOR]`)

		if !inBlockComment && blockCommentOpen.MatchString(line) {
			line = blockCommentOpen.ReplaceAllString(line, `[COLOR="Green"]$0`)
			inBlockComment = true
		}
		if inBlockComment {
			processSpecial = false
			processCommon = false
			if blockCommentClose.MatchString(line) {
				line = blockCommentClose.ReplaceAllString(line, `$0[/COLOR]`)
				inBlockComment = false
				processSpecial = true
				processCommon = true
			}
		}

		if processSpecial {
			if comment.MatchString(line) {
				line = comment.ReplaceAllString(line, `[COLOR="Green"]$0[/COLOR]`)
				processCommon = false
			} else if directive.MatchString(line) {
				line = directive.ReplaceAllString(line, `[COLOR="Blue"]$0[/COLOR]`)
				processCommon = false
			} else {
				processCommon = true
			}
		}

		if processCommon {
			for _, set := range replacements {
				line = regexp.MustCompile(set[0]).
					ReplaceAllString(line, set[1])
			}
		}

		tmp := 0
		for _, ch := range line {
			tmp++
			if ch == '\t' {
				buf.WriteRune(' ')
				for tmp%4 != 0 {
					buf.WriteRune(' ')
					tmp++
				}
			} else {
				buf.WriteRune(ch)
			}
		}
		buf.WriteRune('\n')
	}

	return buf.String()
}
