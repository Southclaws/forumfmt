package markdown

import (
	"bytes"
	"fmt"
	"io"
	"io/ioutil"
	"regexp"
	"strings"

	"github.com/Jeffail/gabs"
	"github.com/russross/blackfriday"
	"github.com/yhat/scrape"
	"golang.org/x/net/html"
	"golang.org/x/net/html/atom"
)

var (
	TagsMyBB = `"tags": {
		"H1": "[COLOR=#FF4700][SIZE=21][B]%s[/B][/SIZE][/COLOR]",
		"H2": "[COLOR=RoyalBlue][SIZE=18][B]%s[/B][/SIZE][/COLOR]",
		"H3": "[COLOR=DeepSkyBlue][SIZE=15][B]%s[/B][/SIZE][/COLOR]",
		"H4": "[COLOR=SlateGray][SIZE=12]%s[/SIZE][/COLOR]"
	}`
	TagsDefault = `"tags": {
		"H1": "[COLOR=#FF4700][SIZE=7][B]%s[/B][/SIZE][/COLOR]",
		"H2": "[COLOR=RoyalBlue][SIZE=6][B]%s[/B][/SIZE][/COLOR]",
		"H3": "[COLOR=DeepSkyBlue][SIZE=5][B]%s[/B][/SIZE][/COLOR]",
		"H4": "[COLOR=SlateGray][SIZE=4]%s[/SIZE][/COLOR]"
	}`
	GeneralSyntax = `{
	%s,
	"keywords": {
		"if": "[COLOR=Blue]$0[/COLOR]",
		"else": "[COLOR=Blue]$0[/COLOR]",
		"for": "[COLOR=Blue]$0[/COLOR]",
		"foreach": "[COLOR=Blue]$0[/COLOR]",
		"while": "[COLOR=Blue]$0[/COLOR]",
		"do": "[COLOR=Blue]$0[/COLOR]",
		"switch": "[COLOR=Blue]$0[/COLOR]",
		"case": "[COLOR=Blue]$0[/COLOR]",
		"default": "[COLOR=Blue]$0[/COLOR]",
		"new": "[COLOR=Blue]$0[/COLOR]",
		"enum": "[COLOR=Blue]$0[/COLOR]",
		"return": "[COLOR=Blue]$0[/COLOR]",
		"continue": "[COLOR=Blue]$0[/COLOR]",
		"break": "[COLOR=Blue]$0[/COLOR]",
		"goto": "[COLOR=Blue]$0[/COLOR]",
		"char": "[COLOR=Blue]$0[/COLOR]",

		"state": "[COLOR=Orange]$0[/COLOR]",

		"true": "[COLOR=Purple]$0[/COLOR]",
		"false": "[COLOR=Purple]$0[/COLOR]",

		"stock": "[COLOR=DeepSkyBlue]$0[/COLOR]",
		"public": "[COLOR=DeepSkyBlue]$0[/COLOR]",
		"forward": "[COLOR=DeepSkyBlue]$0[/COLOR]",
		"const": "[COLOR=DeepSkyBlue]$0[/COLOR]",
		"static": "[COLOR=DeepSkyBlue]$0[/COLOR]",
		"hook": "[COLOR=Blue]$0[/COLOR]"
	},
	"numbers": "[COLOR=Purple]$0[/COLOR]",
	"directives": "[COLOR=Blue]$0[/COLOR]",
	"operators": "[COLOR=Red]$0[/COLOR]",
	"strings": "[COLOR=Purple]$0[/COLOR]",
	"comment_open": "[COLOR=Green]",
	"comment_close": "[/COLOR]"
}`
)

func ParseStyles(styler, tags string) (*gabs.Container, error) {
	var (
		jsonParsed *gabs.Container
		err        error
	)

	if styler == "" {
		jsonParsed, err = gabs.ParseJSON([]byte(fmt.Sprintf(GeneralSyntax, tags)))
	} else {
		jsonParsed, err = gabs.ParseJSONFile("./" + styler + ".json")
	}
	if err != nil {
		return nil, err
	}
	return jsonParsed, nil
}

func Process(input io.Reader, output io.Writer, jsonParsed *gabs.Container) (err error) {
	contents, err := ioutil.ReadAll(input)
	if err != nil {
		return
	}
	contents = bytes.Replace(contents, []byte("\r"), []byte(""), -1)

	out := blackfriday.Run(contents)
	//fmt.Fprint(output, string(out))
	reader := bytes.NewReader(out)

	root, err := html.Parse(reader)
	if err != nil {
		return
	}

	doc := root.FirstChild.LastChild // <html> <head /> <body> (this gets us here) </body>

	styleH1 := jsonParsed.Path("tags.H1").Data().(string)
	styleH2 := jsonParsed.Path("tags.H2").Data().(string)
	styleH3 := jsonParsed.Path("tags.H3").Data().(string)
	styleH4 := jsonParsed.Path("tags.H4").Data().(string)

	forChildren(doc, func(node *html.Node) {
		if scrape.ByTag(atom.H1)(node) {
			fmt.Fprintf(output, styleH1, getText(node, jsonParsed))
		} else if scrape.ByTag(atom.H2)(node) {
			fmt.Fprintf(output, styleH2, getText(node, jsonParsed))
		} else if scrape.ByTag(atom.H3)(node) {
			fmt.Fprintf(output, styleH3, getText(node, jsonParsed))
		} else if scrape.ByTag(atom.H4)(node) {
			fmt.Fprintf(output, styleH4, getText(node, jsonParsed))
		} else if scrape.ByTag(atom.P)(node) {
			fmt.Fprint(output, strings.Replace(getText(node, jsonParsed), "\n", " ", -1))
		} else if scrape.ByTag(atom.Ul)(node) {
			fmt.Fprintf(output, "[LIST]"+getText(node, jsonParsed)+"[/LIST]")
		} else if scrape.ByTag(atom.Ol)(node) {
			fmt.Fprintf(output, "[LIST=1]"+getText(node, jsonParsed)+"[/LIST]")
		} else if scrape.ByTag(atom.Blockquote)(node) {
			fmt.Fprintf(output, "[QUOTE]\n"+getText(node, jsonParsed)+"\n[/QUOTE]")
		} else if scrape.ByTag(atom.Pre)(node) {
			fmt.Fprint(output, getText(node, jsonParsed))
		} else {
			return
		}
		fmt.Fprintf(output, "\n\n")
	})

	return
}

func forChildren(node *html.Node, fn func(node *html.Node)) {
	for c := node.FirstChild; c != nil; c = c.NextSibling {
		fn(c)
	}
}

func getText(node *html.Node, jsonParsed *gabs.Container) string {
	buf := bytes.Buffer{}

	forChildren(node, func(inner *html.Node) {
		if inner.Type == html.TextNode {
			buf.WriteString(inner.Data)
		} else if inner.Type == html.ElementNode {
			begin := ""
			end := ""
			text := getText(inner, jsonParsed)

			if inner.Data == "code" {
				if hasAttr(inner, "class") {
					if attrIs(inner, "class", "language-json") {
						begin = "[PHP]\n"
						end = "[/PHP]"
					} else if attrIs(inner, "class", "language-pawn") {
						begin = `[QUOTE][FONT=Courier New]` + "\n"
						text = Syntax(strings.TrimSpace(text), jsonParsed)
						end = "[/FONT][/QUOTE]"
					} else {
						begin = "[CODE]\n"
						end = "[/CODE]"
					}
				} else {
					begin = `[FONT=courier new]`
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
				end = ""
			} else if inner.Data == "a" {
				href := getAttr(inner, "href")
				if href != "" {
					begin = fmt.Sprintf(`[URL=%s]`, href)
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
			} else if inner.Data == "ul" {
				begin = "[LIST]"
				end = "[/LIST]"
			} else {
				begin = "[UNHANDLED-TAG= + inner.Data + ]"
				end = "[/UNHANDLED-TAG= + inner.Data + ]"
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

func Syntax(in string, jsonParsed *gabs.Container) string {
	stringLiteral := regexp.MustCompile(`"[\s\S]*"`)
	comment := regexp.MustCompile(`//.*`)
	blockCommentOpen := regexp.MustCompile(`\/\*.*`)
	blockCommentClose := regexp.MustCompile(`.*\*\/`)
	directive := regexp.MustCompile(`#.*`)

	styleCommentOpen := jsonParsed.Path("comment_open").Data().(string)
	styleCommentClose := jsonParsed.Path("comment_close").Data().(string)
	styleDirectives := jsonParsed.Path("directives").Data().(string)
	styleNumbers := jsonParsed.Path("numbers").Data().(string)
	styleStrings := jsonParsed.Path("strings").Data().(string)
	//styleOperators := jsonParsed.Path("operators").Data().(string)

	replacements := [][2]string{
		{`0x(\d|[a-f]|[A-F])+`, styleNumbers},
		{`0b([0-1])+`, styleNumbers},
		{`(\+|-)?\d+`, styleNumbers},
	}

	children, err := jsonParsed.Path("keywords").ChildrenMap()
	if err != nil {
		fmt.Println("Failed to read `keywords` from JSON for syntax:", err)
		return in
	}
	for key, child := range children {
		replacements = append(replacements, [2]string{`\b` + key + `\b`, child.Data().(string)})
	}

	processSpecial := true
	processCommon := true
	inBlockComment := false
	buf := bytes.Buffer{}
	var pos []int
	var firstPart, secondPart string

	for _, line := range strings.Split(in, "\n") {
		line = stringLiteral.ReplaceAllString(line, styleStrings)

		if !inBlockComment && blockCommentOpen.MatchString(line) {
			line = blockCommentOpen.ReplaceAllString(line, styleCommentOpen+`$0`)
			inBlockComment = true
		}
		if inBlockComment {
			processSpecial = false
			processCommon = false
			if blockCommentClose.MatchString(line) {
				line = blockCommentClose.ReplaceAllString(line, `$0`+styleCommentClose)
				inBlockComment = false
				processSpecial = true
				processCommon = true
			}
		}

		if processSpecial {
			pos = comment.FindStringIndex(line)

			if pos != nil {
				line = comment.ReplaceAllString(line, styleCommentOpen+`$0`+styleCommentClose)
				processCommon = true
			} else if directive.MatchString(line) {
				line = directive.ReplaceAllString(line, styleDirectives)
				processCommon = false
			} else {
				processCommon = true
			}
		}

		if processCommon {
			if pos != nil {
				firstPart = line[:pos[0]]
				secondPart = line[pos[0]:]
			} else {
				firstPart = line
				secondPart = ""
			}

			for _, set := range replacements {
				firstPart = regexp.MustCompile(set[0]).
					ReplaceAllString(firstPart, set[1])
			}

			line = firstPart + secondPart
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
