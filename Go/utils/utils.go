package utils

import "fmt"
import "time"

func TimeTrack(start time.Time, name string) {
	elapsed := time.Since(start)
	fmt.Printf("%s in .............. %v \n", name, elapsed)
}
