
var renderChart = function(chartData) {
    var ctx = document.getElementById("klocworkTrendChart").getContext("2d");
    var lineChart = new Chart(ctx, {
        type: 'line',
        data: chartData,
        options: {
            responsive: true,
            title: {
                display: true,
                text: 'Klocwork Issue Trend'
            },
            tooltips: {
                mode: 'index',
                intersect: false,
            },
            hover: {
                mode: 'nearest',
                intersect: true
            },
            scales: {
                yAxes: [{
                    display: true,
                    scaleLabel: {
                        display: true,
                        labelString: 'Issues'
                    },
                    ticks: {
                        callback: function(value) {if (value % 1 === 0) {return value;}}
                    }
                }]
            }
        }
    });

    console.log(chartData)
    // var chart = c3.generate({
    //     bindto: '#klocworkTrendChart',
    //     data: chartData,
    //     size: {
    //         height: 350,
    //         width: 600
    //     }
    //     // data: {
    //     //     columns: [
    //     //         ['data1', 30, 200, 100, 400, 150, 250],
    //     //         ['data2', 50, 20, 10, 40, 15, 25]
    //     //     ]
    //     // }
    // });
}
