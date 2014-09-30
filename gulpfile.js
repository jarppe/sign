/* jshint node: true */
var gulp = require('gulp');
var plumber = require('gulp-plumber');
var gprint = require('gulp-print');

// Common
var less = require('gulp-less');
function compileLess() {
  return gulp.src('src/web/foozzaa.less')
    .pipe(plumber())
    .pipe(less({
      paths: ['src/web'],
    }))
    .pipe(gulp.dest('resources/public'));
}

// Dev
var livereload = require('gulp-livereload');
var watch = require('gulp-watch');

gulp.task('less', function() {
  return compileLess();
});

gulp.task('copy-fonts', function() {
  return gulp
    .src(['src/web/components/font-awesome/fonts/*'])
    .pipe(gulp.dest('resources/public/fonts'));
});

gulp.task('copy-polyfills', function() {
  return gulp
    .src(['src/web/components/console-polyfill/index.js',
          'src/web/components/es5-shim/es5-sham.min.js',
          'src/web/components/es5-shim/es5-shim.min.js'])
    .pipe(gulp.dest('resources/public/shim'));
});

gulp.task('default', ['less', 'copy-fonts', 'copy-polyfills'], function() {
  gulp.watch('src/web/**/*.less', ['less']);
  livereload();
  watch({glob: 'resources/**/*.{css,js}', emitOnGlob: false}, function(files) {
    return files
      .pipe(gprint())
      .pipe(livereload());
  });
});

// Build dist
gulp.task('dist', ['less', 'copy-fonts', 'copy-polyfills']);

