var gulp = require('gulp');
var webpack = require('webpack-stream');

var path = require("path"),
    HtmlWebpackPlugin = require('html-webpack-plugin'),
    CopyWebpackPlugin = require('copy-webpack-plugin');

/* AUTOMATED DEPLOY */
function callWebPackProd(output) {
    var dist = output + '/static';
    console.log('webpack dist: ', dist);
    gulp.src(["./src/js/controller.js", "./src/js/app.js"])
    .pipe(
        webpack(
            {
                entry: {
                    controller: path.resolve(__dirname, "./src/js/controller.js"),
                    app: path.resolve(__dirname, "./src/js/app.js")
                },
                output: {
                    path: dist,
                    filename: "[name].bundle.js",
                    publicPath: ''
                },
                module: {
                    loaders: [
                        { test: /\.css$/, loader: "style!css" },
                        { test: /\.less$/, loader: "style!css!less" },
                        {
                            test: /\.jsx?$/,
                            exclude: /node_modules/,
                            loader: 'babel',
                            query: {
                                presets: ['es2015', 'react']
                            }
                        },
                        {test: /\.(jpe?g|png|gif|svg)$/i, loader: 'url' },
                        {test: /\.(woff|woff2)(\?v=\d+\.\d+\.\d+)?$/, loader: 'url?mimetype=application/font-woff'},
                        {test: /\.ttf(\?v=\d+\.\d+\.\d+)?$/, loader: 'url?mimetype=application/octet-stream'},
                        {test: /\.eot(\?v=\d+\.\d+\.\d+)?$/, loader: 'file'},
                        {test: /\.svg(\?v=\d+\.\d+\.\d+)?$/, loader: 'url?mimetype=image/svg+xml'}
                    ]
                },
                resolve: {
                    extensions: [ '', '.js', '.jsx' ]
                },
                plugins: [
                    new HtmlWebpackPlugin({
                        filename: "controller.html",
                        template: "./src/html/controller.html",
                        inject: false
                    }),
                    new HtmlWebpackPlugin({
                        filename: "app.html",
                        template: "./src/html/app.html",
                        inject: false
                    }),
                    new CopyWebpackPlugin([
                        { from: './src/img', to: 'img' }
                    ]),
                    new CopyWebpackPlugin([
                        { from: './src/img', to: 'img' }
                    ]),
                    new CopyWebpackPlugin([
                        { from: './bundle.json', to: 'bundle' }
                    ])
                ]
            },
            null
        )
    ).pipe(gulp.dest(dist));
    console.log('callWebPack ',dist);
}

gulp.task('build', function callWatchTasks() {
    var output, i = process.argv.indexOf("--output-path");

    if (i == -1) {
        console.log("You should define the output path. Usage: gulp build --output-path PATH");
        process.exit(1);
    }

    output = process.argv[i+1];

    callWebPackProd(output);
});