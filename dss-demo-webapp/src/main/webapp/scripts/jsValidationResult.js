$( document ).ready(function() {
    $('#detailed-report-card > .card-header').append($('#detailed-report-buttons'));
    $('#detailed-report-buttons').addClass('inside-card-header');
});

$('#detailed-report-buttons .btn').click(function(e) {
    e.stopPropagation();
    if ($(this).hasClass("switch-annotations")) {
        switchAnnotations($(this), e);
    }
});

$('.id-copy').click(function(e) {
    e.stopPropagation();
    copyToClipboard($(this));
});

function switchAnnotations($this, e) {
    if ($this.hasClass("show")) {
        $('#detailed-report .constraint-text').addClass('d-none');
        $('#detailed-report .card-body .constraint .constraint-text').removeClass('d-block');
        $('#detailed-report .constraint-tooltip').removeClass('d-none');
        $this.find('.icon').addClass('fa-commenting-o');
        $this.find('.icon').removeClass('fa-comment-o');
        $this.find('.label-show').removeClass('d-none');
        $this.find('.label-hide').addClass('d-none');
        $this.removeClass('show');
    } else {
        $('#detailed-report .constraint-text').removeClass('d-none');
        $('#detailed-report .card-body .constraint .constraint-text').addClass('d-block');
        $('#detailed-report .constraint-tooltip').addClass('d-none');
        $this.find('.icon').removeClass('fa-commenting-o');
        $this.find('.icon').addClass('fa-comment-o');
        $this.find('.label-show').addClass('d-none');
        $this.find('.label-hide').removeClass('d-none');
        $this.addClass('show');
    }
}

function copyToClipboard($this) {
    var $temp = $('<input>');
    $("body").append($temp);
    $temp.val($this.data('id')).select();
    document.execCommand("copy");
    $temp.remove();

    var oldValue = $this.data('original-title');
    var newValue = $this.data('success-text');
    $this.attr('data-original-title', newValue);
    $this.tooltip('show');

    $this.mouseout(function() {
        $this.attr('data-original-title', oldValue);
    });
}