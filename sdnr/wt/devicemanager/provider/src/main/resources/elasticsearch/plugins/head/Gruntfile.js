/**
 * Copyright 2010-2013 Ben Birch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this software except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
module.exports = function(grunt) {

	var fileSets = require("./grunt_fileSets.js");

	// Project configuration.
	grunt.initConfig({
		clean: {
			_site: {
				src: ['_site']
			}
		},
		concat: {
			vendorjs: {
				src: fileSets.vendorJs,
				dest: '_site/vendor.js'
			},
			vendorcss: {
				src: fileSets.vendorCss,
				dest: '_site/vendor.css'
			},
			appjs: {
				src: fileSets.srcJs,
				dest: '_site/app.js'
			},
			appcss: {
				src: fileSets.srcCss,
				dest: '_site/app.css'
			}
		},

		copy: {
			site_index: {
				src: 'index.html',
				dest: '_site/index.html',
				options: {
					process: function( src ) {
						return src.replace(/_site\//g, "");
					}
				}
			},
			base: {
				expand: true,
				cwd: 'src/app/base/',
				src: [ '*.gif', '*.png', '*.css' ],
				dest: '_site/base/'
			},
			iconFonts: {
				expand: true,
				cwd: 'src/vendor/font-awesome/fonts/',
				src: '**',
				dest: '_site/fonts'
			},
			i18n: {
				src: 'src/vendor/i18n/i18n.js',
				dest: '_site/i18n.js'
			},
			lang: {
				expand: true,
				cwd: 'src/app/lang/',
				src: '**',
				dest: '_site/lang/'
			}
		},

		jasmine: {
			task: {
				src: [ fileSets.vendorJs, 'src/vendor/i18n/i18n.js', 'src/app/lang/en_strings.js', fileSets.srcJs ],
				options: {
					specs: 'src/app/**/*Spec.js',
					helpers: 'test/spec/*Helper.js',
					display: "short",
					summary: true
				}
			}
		},

		watch: {
			"scripts": {
				files: ['src/**/*', 'test/spec/*' ],
				tasks: ['default'],
				options: {
					spawn: false
				}
			},
			"grunt": {
				files: [ 'Gruntfile.js' ]
			}
		},

		connect: {
			server: {
				options: {
					port: 9100,
					base: '.',
					keepalive: true
				}
			}
		}

	});

	grunt.loadNpmTasks('grunt-contrib-clean');
	grunt.loadNpmTasks('grunt-contrib-concat');
	grunt.loadNpmTasks('grunt-contrib-watch');
	grunt.loadNpmTasks('grunt-contrib-connect');
	grunt.loadNpmTasks('grunt-contrib-copy');
	grunt.loadNpmTasks('grunt-contrib-jasmine');

	// Default task(s).
	grunt.registerTask('default', ['clean', 'concat', 'copy', 'jasmine']);
	grunt.registerTask('server', ['connect:server']);
	grunt.registerTask('dev', [ 'default', 'watch' ]);


};
