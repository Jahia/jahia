'use strict';

const { task, watch, series, src, dest } = require('gulp');
const uglify = require('gulp-uglify');
const javascriptObfuscator = require('gulp-javascript-obfuscator');
const concat = require('gulp-concat');
const log = require('fancy-log');
const argv = require('minimist')(process.argv.slice(2));
const sass = require('gulp-sass');
const sourcemaps = require('gulp-sourcemaps');
const copy = require('gulp-copy');
const rename = require('gulp-rename');

// Global path
const anthraciteRootDir = 'src/main/webapp/engines/jahia-anthracite';

// Initialize sass compiler
sass.compiler = require('node-sass');

// sass path
const sassSrc = anthraciteRootDir + '/css/**/*.scss';
const cssDirDest = anthraciteRootDir + '/css';

// javascript path
const jsSrc = anthraciteRootDir + '/js';
const jsDirDest = jsSrc + '/dist';
const jsDirDestBuild = jsDirDest + '/build';

const jsFiles = [
    jsSrc + '/dependencies/polyfills.js',
    jsSrc + '/dependencies/jGet.js',
    jsSrc + '/Anthracite.js',
    jsSrc + '/contextMenus.js',
    jsSrc + '/backgroundJobs.js',
    jsSrc + '/picker.js',
    jsSrc + '/imagePreview.js',
    jsSrc + '/engine.js',
    jsSrc + '/workflow.js',
    jsSrc + '/iframe.js',
    jsSrc + '/admin.js',
    jsSrc + '/modals.js',
    jsSrc + '/dialog.js',
    jsSrc + '/userPickers.js',
    jsSrc + '/remote.js',
    jsSrc + '/edit.js',
    jsSrc + '/dashboard.js',
    jsSrc + '/contribute.js',
    jsSrc + '/localisedStrings.js',
    jsSrc + '/listeners.js',
    jsSrc + '/init.js'];

task('sass', done => {
    src(sassSrc).pipe(sourcemaps.init())
        .pipe(sass().on('error', sass.logError))
        .pipe(sourcemaps.write('.'))
        .pipe(dest(cssDirDest));
    done();
});

task('build-js', done => {
    src(jsFiles).pipe(concat('anthracite.js'))
        .pipe(dest(jsDirDest));
    done();
});

task('copy-js-to-build', done => {
    src([jsDirDest + '/anthracite.js'])
        .pipe(copy(jsDirDestBuild))
        .pipe(rename('anthracite-min.js'))
        .pipe(dest(jsDirDestBuild));
    done();
});

task('minify-js', done => {
    src([jsDirDestBuild + '/anthracite-min.js'])
        .pipe(sourcemaps.init())
        .pipe(uglify())
        .pipe(javascriptObfuscator({
            compact: true
        }))
        .pipe(sourcemaps.write('.'))
        .pipe(dest(jsDirDestBuild));
    done();
});

/**
 * For some reason gulp is going faster than the file system
 * so we need to delay a little bit the tasks execution
 *
 * @param done      callback function
 * @param timeout   time to wait
 */
const wait = (done, timeout) => {
    setTimeout( ()=> {
        done();
    }, timeout);
};

task('wait', done => wait(done, 1000));

task('wait-3s', done => wait(done, 3000));

task('copy-js-to-webapp', done => {
    const targetDist = argv.dest + '/engines/jahia-anthracite/js/dist';
    const targetDistBuild = targetDist + '/build';

    log.info('Copying JS file to local server: ' + targetDist);
    src(jsDirDest + '/*').pipe(dest(targetDist));

    log.info('Copying JS files to local server: ' + targetDistBuild);
    src(jsDirDestBuild + '/*').pipe(dest(targetDistBuild));
    done();
});

task('copy-css-to-webapp', done => {
    const targetDirectory = argv.dest + '/engines/jahia-anthracite/css';

    log.info('Copying CSS file to local server: ' + targetDirectory);
    src(cssDirDest+ '/*.css*').pipe(dest(targetDirectory));
    done();
});

task('watch', done => {
    if (!argv.dest) {
        log.error('--dest option not set (e.g: --dest=/path/to/your/tomcat/webapps/yourWebAppName)');
        done();
        return;
    }

    watch(jsFiles, series('generate-js', 'wait-3s', 'copy-js-to-webapp'));

    watch(sassSrc, series('sass', 'wait', 'copy-css-to-webapp'));

    log.info('Gulp is watching JS and SASS files');
    log.info('Happy Coding!');
});

task('generate-js', series('build-js', 'wait', 'copy-js-to-build', 'wait', 'minify-js'));
