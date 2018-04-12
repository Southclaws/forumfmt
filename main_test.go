package main

import (
	"bytes"
	"io/ioutil"
	"os"
	"path/filepath"
	"strings"
	"testing"

	"github.com/stretchr/testify/assert"
)

func Test_process(t *testing.T) {
	files, err := ioutil.ReadDir("tests")
	if err != nil {
		panic(err)
	}

	for _, file := range files {
		if filepath.Ext(file.Name()) != ".md" {
			continue
		}

		mdFile := filepath.Join("tests", file.Name())
		bbFile := strings.TrimSuffix(mdFile, filepath.Ext(file.Name())) + ".bb"

		input, err := os.Open(mdFile)
		if err != nil {
			panic(err)
		}
		output := bytes.NewBuffer(nil)
		wantOutput, err := ioutil.ReadFile(bbFile)
		if err != nil {
			panic(err)
		}

		err = process(input, output)
		if err != nil {
			t.Error(err)
		}

		assert.Equal(t, output.String(), string(wantOutput))
	}
}

func Test_syntax(t *testing.T) {
	type args struct {
		in string
	}
	tests := []struct {
		name string
		args args
		want string
	}{
		{"percent", args{`printf("%s", str);`}, `printf([COLOR="Purple"]"%s"[/COLOR], str);` + "\n"},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			got := syntax(tt.args.in)
			assert.Equal(t, tt.want, got)
		})
	}
}
