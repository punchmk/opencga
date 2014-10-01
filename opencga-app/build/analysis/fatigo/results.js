var RESULT = {
    "fatigo.default": {
        "layout": {
            "title": "Job results",
            "presentation": "custom",
            "children": function () {
                console.log(this.xml);

                var tableRenderer = {
                    type: 'file-paging-grid',
                    width: '100%',
                    height: 300,
                    tableLayout: {
                        fields: ["term", "term_size", "term_size_in_genome", "list1_positives", "list1_negatives", "list1_percentage", "list2_positives", "list2_negatives", "list2_percentage", "list1_positive_ids", "list2_positive_ids", "odds_ratio_log", "pvalue", "adj_pvalue"],
                        columns: [
                            {header: "term", dataIndex: "term"},
                            {header: "term_size", dataIndex: "term_size"},
                            {header: "term_size_in_genome", dataIndex: "term_size_in_genome"},
                            {header: "list1_positives", dataIndex: "list1_positives"},
                            {header: "list1_negatives", dataIndex: "list1_negatives"},
                            {header: "list1_percentage", dataIndex: "list1_percentage"},
                            {header: "list2_positives", dataIndex: "list2_positives"},
                            {header: "list2_negatives", dataIndex: "list2_negatives"},
                            {header: "list2_percentage", dataIndex: "list2_percentage"},
                            {header: "list1_positive_ids", dataIndex: "list1_positive_ids"},
                            {header: "list2_positive_ids", dataIndex: "list2_positive_ids"},
                            {header: "odds_ratio_log", dataIndex: "odds_ratio_log"},
                            {header: "pvalue", dataIndex: "pvalue"},
                            {header: "adj_pvalue", dataIndex: "adj_pvalue"}
                        ],
                        types: ["auto", "auto", "auto", "auto", "auto", "auto", "auto", "auto", "auto", "auto", "auto", "auto", "auto", "auto"],
                        visibility: [1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1],
                        order: [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13]
                    }
                };



                var groupMap = {
                    "0.005": [],
                    "0.01": [],
                    "0.05": [],
                    "0.1": []
                };
//                var groupList = ['0.05', '0.005', '0.01', '0.1'];
                var groupList = ['0.05', '0.01', '0.1'];
                for (var i = 0, leni = this.outputItems.length; i < leni; i++) {
                    var fileName = this.outputItems[i];
                    for (key in groupMap) {
                        if (fileName.indexOf(key) !== -1 && fileName.indexOf('go') !== -1) {
                            groupMap[key].push(fileName);
                        }
                    }
                }


                var tabs = [];

                for (var i = 0, leni = groupList.length; i < leni; i++) {
                    var group = groupList[i];

                    var items = [];
                    items.push({
                        xtype: 'box',
                        cls: 'ocb-panel-title',
                        html: 'Sumary'
                    }, {
                        'title': 'Number of significant terms per DB',
                        'file': 'significant_count_' + group + '.txt',
                        "renderers": [
                            {header: true, type: 'table'}
                        ]
                    });
                    for (var j = 0; j < groupMap[group].length; j++) {
                        var fileName = groupMap[group][j];
                        var subgraphFile = fileName.split('_').slice(1,6).join('_')+'.txt';

                        items.push({
                            'title': fileName,
                            'file': fileName,
                            "renderers": [
                                tableRenderer,
                                {
                                    width: 900,
                                    height: 500,
                                    type: 'network-viewer',
                                    utils: {
                                        name: '/network/go-subgraph',
                                        params: {
                                            input: this.job.command.data.outdir+"/"+subgraphFile,
                                            adjustedPvalue: group
                                        },
                                        success:function(data, networkViewer){
//                                            if (data.indexOf("ERROR") != -1) {
//                                                console.error(data);
//                                            }


                                            var textNetworkDataAdapter = new TextNetworkDataAdapter({
                                                dataSource: new StringDataSource(data.response.sif),
                                                handlers: {
                                                    'data:load': function (event) {
                                                        var graph = event.sender.parseColumns(0, 1, -1, "link");
                                                        networkViewer.setGraph(graph);
//                                                        networkViewer.setLayout('Force directed');
                                                    },
                                                    'error:parse': function (event) {
                                                        console.log(event.errorMsg);
                                                    }
                                                }
                                            });

                                            var attributeNetworkDataAdapter = new AttributeNetworkDataAdapter({
                                                dataSource: new StringDataSource(data.response.attr),
                                                handlers: {
                                                    'data:load': function (event) {
                                                        var json = event.sender.getAttributesJSON();
                                                        networkViewer.network.importVertexWithAttributes({content: json});

                                                        //TODO

                                                    },
                                                    'error:parse': function (event) {
                                                        console.log(event.errorMsg);
                                                    }
                                                }
                                            });

//                                            console.log('success')
//                                            console.log(data.response);
//                                            if (typeof data.response.error === 'undefined') {
//                                                var attributeNetworkDataAdapter = new AttributeNetworkDataAdapter({
//                                                    dataSource: new StringDataSource(data.response.local),
//                                                    handlers: {
//                                                        'data:load': function (event) {
//                                                            _this.progress.updateProgress(0.4, 'processing data');
//                                                            var json = event.sender.getAttributesJSON();
//                                                            json.attributes[1].name = "Degree";
//                                                            json.attributes[2].name = "Betweenness";
//                                                            json.attributes[3].name = "Closeness centrality";
//                                                            json.attributes[4].name = "Clustering coefficient";
//                                                            console.log(json)
//                                                            _this.progress.updateProgress(0.7, 'creating attributes');
//                                                            _this.cellMaps.networkViewer.importVertexWithAttributes({content: json});
//                                                            _this.progress.updateProgress(1, 'Topology information retrieved successfully');
//                                                            _this.resultContainer.show()
//                                                        }
//                                                    }
//                                                });
//                                                _this.globalResult.update(Utils.htmlTable(data.response.global));
//                                            } else {
//                                                _this.progress.updateProgress(0, data.response.error);
//                                            }
                                        }
                                    }
                                }
                            ]
                        })
                    }
                    tabs.push({
                        title: group,
                        items: items
                    });
                }

                return {
                    xtype: 'container',
                    items: [
                        {
                            xtype: 'box',
                            cls: 'ocb-panel-title',
                            html: 'Choose your adjusted pvalue thereshold:'
                        },
                        {
                            xtype: 'tabpanel',
                            plain: true,
                            items: tabs
                        }
                    ]

                };
            },
            oldXML: 'result.xml'
        }
    }
};
