'use strict';

const gulp = require('gulp');
const minify = require('gulp-minify');
const uglify = require('gulp-uglify');
const javascriptObfuscator = require('gulp-javascript-obfuscator');
const concat = require('gulp-concat');

const jsFiles = ['js/dependencies.js', 'js/methods.js', 'js/eventListeners.js', 'js/localisedString.js', 'js/edit.js'];
const jsDest = 'js/dist';

gulp.task('concat', () => {
    return gulp.src(jsFiles)
        .pipe(concat('anthracite.js'))
        .pipe(gulp.dest(jsDest));
});

gulp.task('build', done => {
    gulp.src(['js/dist/anthracite.js'])
        .pipe(minify())
        .pipe(uglify())
        .pipe(javascriptObfuscator({
            compact: true
        }))
        .pipe(gulp.dest('./js/dist/build/'));
    done();
});

gulp.task('generate', gulp.series('concat', 'build'));
