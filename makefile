TESTS = $(shell ls -S `find test -type f -name "*.test.js" -print`)
REPORTER = spec
TIMEOUT = 3000
MOCHA_OPTS =

clean:
	@rm -rf node_modules

install:
	@npm install

test: install
	@NODE_ENV=test ./node_modules/.bin/mocha \
	--bail \
	--reporter $(REPORTER) \
	--timeout $(TIMEOUT) \
	$(MOCHA_OPTS) \
	$(TESTS)

test-cov: install
	@NODE_ENV=test node ./node_modules/.bin/istanbul cover --report html \
	./node_modules/.bin/_mocha -- \
	--reporter $(REPORTER) \
	--timeout $(TIMEOUT) \
	$(MOCHA_OPTS) \
	$(TESTS)

