<%@tag pageEncoding="UTF-8"%>
<%@attribute name="title" required="true"%>
<%@attribute name="description" required="true"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="description" content="${description}">
    <meta name="author" content="psjava team">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <title>${title}</title>
    <link rel="stylesheet" media="screen" href="/bootstrap-3.1.1-customized-9598904/css/bootstrap.min.css">
    <link rel="stylesheet" media="screen" href="/stylesheets/main.css">
</head>
<body style="padding-top: 60px; margin-bottom:100px">
<script src="/jquery-1.11.0.min.js"></script>
<script src="/bootstrap-3.1.1-customized-9598904/js/bootstrap.min.js"></script>
<div class="container">
    <nav class="navbar navbar-default navbar-fixed-top" role="navigation">
        <div class="container">
            <div class="navbar-header">
                <button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#id-navbar-collapse">
                    <span class="sr-only">Toggle navigation</span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                </button>
                <a class="navbar-brand" href="/">psjava</a>
            </div>
            <div class="collapse navbar-collapse" id="id-navbar-collapse">
                <ul class="nav navbar-nav">
                    <li><a href="/#title-download">Download</a></li>
                    <li><a href="/#title-getting-started">Getting Started</a></li>
                    <li><a href="/#title-algorithms">Algorithms</a></li>
                    <li><a href="/#title-data-structures">Data Structures</a></li>
                    <li><a href="https://github.com/psjava/psjava">View on GitHub</a></li>
                </ul>
                <!--
                <form class="navbar-form navbar-right" role="search">
                    <div class="form-group">
                        <input type="text" class="form-control" placeholder="Search">
                    </div>
                    <button type="submit" class="btn btn-default">Submit</button>
                </form>
                -->
            </div>
        </div>
    </nav>
    <jsp:doBody/>
    <!--
    <div class="row">
        <div class="col-md-2">
            <ul class="nav nav-pills nav-stacked">
                <li><a href="/#title-download">Download</a></li>
                <li><a href="/#title-getting-started">Getting Started</a></li>
                <li><a href="/#title-algorithms">Algorithms</a></li>
                <li><a href="/#title-data-structures">Data Structures</a></li>
                <li><a href="https://github.com/psjava/psjava">View on GitHub</a></li>
            </ul>
        </div>
    </div>
    -->
    <hr>
    <p class="text-center"><small>Copyright 2014 psjava team. <a href="https://github.com/psjava/psjava">View on GitHub</a></small></p>
    <p class="text-center">
        <iframe src="http://ghbtns.com/github-btn.html?user=psjava&repo=psjava&type=watch&count=true" allowtransparency="true" frameborder="0" scrolling="0" width="110" height="20"></iframe>
        <iframe src="http://ghbtns.com/github-btn.html?user=psjava&repo=psjava&type=fork&count=true" allowtransparency="true" frameborder="0" scrolling="0" width="95" height="20"></iframe>
    </p>
</div>

<!-- http://stackoverflow.com/questions/14248194/close-responsive-navbar-automatically -->
<script>
    $('.navbar-collapse a').click(function() {
        $('.navbar-collapse').collapse('hide');
    });
</script>
<script>
    (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
        (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
            m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
    })(window,document,'script','//www.google-analytics.com/analytics.js','ga');
    ga('create', 'UA-42967591-1', 'psjava.org');
    ga('send', 'pageview');
</script>

</body>
</html>
