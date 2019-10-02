'use strict';

const gulp = require('gulp');
const minify = require('gulp-minify');
const uglify = require('gulp-uglify');
const javascriptObfuscator = require('gulp-javascript-obfuscator');
const concat = require('gulp-concat');
const watch = require('gulp-watch');
const gutil = require('gulp-util');

const anthraciteRootDir = 'src/main/webapp/engines/jahia-anthracite';
const anthraciteJsDir = anthraciteRootDir + '/js';
const jsFiles = [
    anthraciteJsDir + '/dependencies.js',
    anthraciteJsDir + '/methods.js',
    anthraciteJsDir + '/eventListeners.js',
    anthraciteJsDir + '/localisedString.js',
    anthraciteJsDir + '/edit.js'];
const anthraciteJsDistDir = anthraciteJsDir + '/dist';

gulp.task('concat', () => {
    return gulp.src(jsFiles)
        .pipe(concat('anthracite.js'))
        .pipe(gulp.dest(anthraciteJsDistDir));
});

gulp.task('build', done => {
    gulp.src([anthraciteJsDistDir + '/anthracite.js'])
        .pipe(minify())
        .pipe(uglify())
        .pipe(javascriptObfuscator({
            compact: true
        }))
        .pipe(gulp.dest('./' + anthraciteJsDistDir + '/build/'));
    done();
});

gulp.task('watch', () => {
    return watch(anthraciteJsDir + '/*.js', function () {
        const h = new Date().getHours();
        const m = new Date().getMinutes();
        const s = new Date().getSeconds();

        console.log("Build " + h + ':' + m + ':' + s);

        if (gutil.env.dest === undefined) {
            console.log("Dest env variable not set ! Use --dest=");
        } else {
            gulp.src(jsFiles)
                .pipe(concat('anthracite.js'))
                .pipe(gulp.dest(anthraciteJsDistDir));
            gulp.src([anthraciteJsDistDir + '/anthracite.js'])
                .pipe(minify())
                .pipe(uglify())
                .pipe(javascriptObfuscator({
                    compact: true
                }))
                .pipe(gulp.dest(gutil.env.dest));
        }
    })
});

gulp.task('generate', gulp.series('concat', 'build'));
