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

		assert.Equal(t, output.Bytes(), wantOutput)
	}
}
