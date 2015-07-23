V=`git rev-parse --short HEAD`
B="-X main.Version $(V)"

run:
	@godep go run main.go

test:
	@godep go test -p 1 -cover -race -bench=. -benchmem ./...

build:
	@go get github.com/tools/godep
	@godep go build -ldflags=$(B) -o bin/pgsolv

.PHONY: build
