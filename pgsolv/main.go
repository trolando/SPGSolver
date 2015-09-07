package main

import (
	log "github.com/Sirupsen/logrus"
	"github.com/docopt/docopt-go"
)

const usage = `
Usage:
  pgsolv build
  pgsolv solve [options] <arenas>...
  pgsolv bench [options] <arenas>...

Options:
    -h --help       Show this screen.
    --version       Show version.
    -v --verbose    Enable verbose mode.
`

var Version string

func main() {
	args, _ := docopt.Parse(usage, nil, true, Version, false)
	if args["--verbose"].(bool) {
		log.SetLevel(log.DebugLevel)
	}
	log.Debug("Starting")
	log.Info(args)
}
