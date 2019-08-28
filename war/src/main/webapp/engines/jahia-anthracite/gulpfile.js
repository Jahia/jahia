'use strict';

const gulp = require('gulp');
const minify = require('gulp-minify');
const uglify = require('gulp-uglify');
const javascriptObfuscator = require('gulp-javascript-obfuscator');
const concat = require('gulp-concat');
const watch = require('gulp-watch');
const gutil = require('gulp-util');

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

gulp.task('watch', () => {
    return watch('js/*.js', function () {
        const h = new Date().getHours();
        const m = new Date().getMinutes();
        const s = new Date().getSeconds();

        console.log("Build " + h + ':' + m + ':' + s);

        if (gutil.env.dest === undefined) {
            console.log("Dest env variable not set ! Use --dest=");
        } else {
            gulp.src(jsFiles)
                .pipe(concat('anthracite.js'))
                .pipe(gulp.dest(jsDest));
            gulp.src(['js/dist/anthracite.js'])
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
