//Copyright (C) 2020 Alibaba Group Holding Limited
const gulp = require('gulp'),
    clean = require('gulp-clean'),
    webpack = require('webpack');

// 清理
gulp.task('clean', function() {
    return gulp.src(['build'], {
            read: false
        })
        .pipe(clean());
});

gulp.task('dev', ['clean'], function(done) {
    webpack(require('./webpack.dev.config.js'), function(err, stats) {
        console.log(stats.toString());
    });
});

gulp.task('prod', ['clean'], function(done) {
    webpack(require('./webpack.production.config.js'), function(err, stats) {
        console.log(stats.toString());
    });
});
