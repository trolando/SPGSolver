package utils

import "fmt"
import "time"

func TimeTrack(start time.Time, name string) {
	elapsed := time.Since(start)
	fmt.Printf("%s in .............. %.6f seconds\n", name, elapsed.Seconds())
}
