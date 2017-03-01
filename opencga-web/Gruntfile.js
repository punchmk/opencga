
/*
 * Copyright 2015-2016 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

module.exports = function(grunt) {

    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),
        build: {
            path: 'build/<%= pkg.version %>',
            vendor: '<%= build.path %>/vendor'
        },
        clean: {
            dist: ['<%= build.path %>/*']
        },
        jshint: {
            files: ['Gruntfile.js', 'src/**/*.js'],
            options: {
                globals: {
                    jQuery: true
                }
            }
        },
        concat: {
            options: {
                sourceMap: true
            },
            vendors: {
                src: [
                    './bower_components/webcomponentsjs/webcomponents-lite.js',
                    './bower_components/underscore/underscore.js',
                    './bower_components/backbone/backbone.js',
                    './bower_components/jquery/dist/jquery.js',
                    './bower_components/uri.js/src/URI.js',
                    './bower_components/cookies-js/dist/cookies.js',
                    './bower_components/crypto-js/crypto-js.js',
                    './bower_components/jquery-ui/jquery-ui.js'
                ],
                dest: '<%= build.path %>/vendors.js'
            },
            jsorolla: {
                src: [
                    './lib/jsorolla/src/lib/utils.js',
                    './lib/jsorolla/src/lib/clients/rest-client.js',
                    './lib/jsorolla/src/lib/clients/opencga-client-config.js',
                    './lib/jsorolla/src/lib/clients/opencga-client.js'
                ],
                dest: '<%= build.path %>/jsorolla-clients.js'
            }
        },
        uglify: {
            options: {
                banner: '/*! Genomics England <%= grunt.template.today("dd-mm-yyyy") %> */\n'
            },
            dist: {
                files: {
                    '<%= build.path %>/vendors.min.js': ['<%= build.path %>/vendors.js']
                }
            }
        },
        copy: {
            dist: {
                files: [
                    {   flatten: true, expand: true, cwd: './bower_components', src: ['font-awesome/css/font-awesome.min.css'], dest: '<%= build.path %>/css' },
                    {   flatten: true, expand: true, cwd: './bower_components', src: ['jquery-ui/themes/smoothness/jquery-ui.min.css'], dest: '<%= build.path %>/css' },
                    {   flatten: true, expand: true, cwd: './bower_components', src: ['font-awesome/fonts/*'], dest: '<%= build.path' +
                    ' %>/fonts' },
                    {   expand: true, cwd: './bower_components', src: ['polymer/*'], dest: '<%= build.vendor %>' },
                    {   expand: true, cwd: 'src', src: ['index.html'], dest: '<%= build.path %>/' },
                    {   expand: true, cwd: 'src', src: ['config.js'], dest: '<%= build.path %>/' },
                    {   expand: true, cwd: './', src: ['LICENSE'], dest: '<%= build.path %>/' }
                ]
            }
        },
        processhtml: {
            options: {
                strip: true
            },
            dist: {
                files: {
                    '<%= build.path %>/index.html': ['src/index.html']
                }
            }
        },
        vulcanize: {
            default: {
                options: {
                    // Task-specific options go here.
                    stripComments: true,
                    // excludes: ["./bower_components/polymer/polymer.html"]
                },
                files: {
                    '<%= build.path %>/gel-catalog.html': 'src/gel-catalog.html'
                }
            }
        },
        watch: {
            files: ['<%= jshint.files %>'],
            tasks: ['jshint']
        },
        replace: {
            dist: {
                options: {
                    patterns: [
                        {
                            match: /..\/bower_components/g,
                            replacement: 'vendor'
                        }
                    ]
                },
                files: [
                    {expand: true, flatten: true, src: ['<%= build.path %>/index.html'], dest: '<%= build.path %>'},
                    {expand: true, flatten: true, src: ['<%= build.path %>/gel-catalog.html'], dest: '<%= build.path %>'}
                ]
            }
        }
    });

    grunt.loadNpmTasks('grunt-contrib-clean');
    grunt.loadNpmTasks('grunt-contrib-jshint');
    grunt.loadNpmTasks('grunt-contrib-copy');
    grunt.loadNpmTasks('grunt-contrib-concat');
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-processhtml');
    grunt.loadNpmTasks('grunt-vulcanize');
    grunt.loadNpmTasks('grunt-contrib-watch');
    grunt.loadNpmTasks('grunt-replace');

    grunt.registerTask('default', ['clean', 'jshint', 'copy', 'concat', 'uglify', 'processhtml', 'vulcanize']);
    grunt.registerTask('cl', ['clean']);
};