package main

import (
	"bufio"
	"log"
	"os"
	"net/http"
	"io"
)

func main() {
	r := bufio.NewReader(os.Stdin)

	response, err := http.Post(os.Args[1],"text", r)
	if err != nil {
		log.Fatal(err)
	} else {
		_, err := io.Copy(os.Stdout, response.Body)
		response.Body.Close()

		if err != nil {
			log.Fatal(err)
		}
		response.Body.Close()

	}
}
