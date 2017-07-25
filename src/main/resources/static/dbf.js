/**
 * Created by qk on 2017/6/28.
 */
var isOnChange;
var currentCode;

$.ajax({
    type: "get",
    url: "/getSrcPath",
    success: function (data) {
        $("#srcPath").val(data);
    },
    error: function (XMLHttpRequest, textStatus, errorThrown) {
        alert("error");
    }
});

$.ajax({
    type: "get",
    url: "/getDstPath",
    success: function (data) {
        $("#dstPath").val(data);
    },
    error: function (XMLHttpRequest, textStatus, errorThrown) {
        alert("error");
    }
});

$("#codeId").bind("click", function () {
    if (isOnChange) {
        return;
    }
    $.ajax({
        type: "post",
        url: "/code",
        success: function (data) {
            $("#codeId").find("option").remove();
            $("#codeId").append('<option value="请选择股东代码">请选择股东代码</option>');
            var tempList = data.split(',');
            for (tempindex in tempList) {
                $("#codeId").append('<option value="'+tempList[tempindex]+'">'+tempList[tempindex]+"</option>");
            }
        },

        error: function (XMLHttpRequest, textStatus, errorThrown) {
            alert("error");
        }
    });
});

function selectCode(value){
    isOnChange = true;
    currentCode = value.replace(/\s/ig,'');
    $.ajax({
        type: "post",
        url: "/file?code="+currentCode,
        success: function (data) {
            $("#cbarea").empty()
            $("#cbarea").find("input").remove();
            $("#cbarea").find("span").remove();
            $("#cbarea").find("br").remove();
            var tempList = data.split(',');
            for (tempindex in tempList) {
                var tempvalue = tempList[tempindex].replace(/\s/ig,'');
                $("#cbarea").append('<input type="checkbox" name = "cbtest" style="zoom:150%;" value="'
                    + tempvalue +'" <span>'+ tempvalue +'</span></input><br>');
            }
            isOnChange = false;

        },

        error: function (XMLHttpRequest, textStatus, errorThrown) {
            alert("错误");
        }
    });
};

$("#btn1").bind("click", function () {
    var tempurl = "/selectDBF?selectCode="+$.trim(currentCode);
    var chk_value =[];
    $('input[name="cbtest"]:checked').each(function(){
        chk_value.push($(this).val());
    });
    var tempurl = "/selectDBF?selectCode="+$.trim(currentCode) + "&selectFile=" + chk_value;
    $.ajax({
        type: "post",
        url: tempurl,
        success: function (data) {
            var tempList = data.split(',');
            for (tempindex in tempList) {
                $("#unitid").append('<option value="'+tempList[tempindex]+'">'+tempList[tempindex]+"</option>");
            }
            alert("筛选成功");
        },

        error: function (XMLHttpRequest, textStatus, errorThrown) {
            alert("筛选失败");
        }
    });
});

$("#pathBtn1").bind("click", function () {
    var srcPath = $("#srcPath").val();
    srcPath = srcPath.replace(/\\/g,"/");
    var tempurl = "/setSrcPath?srcPath="+srcPath;
    $.ajax({
        type: "post",
        url: tempurl,
        success: function (data) {
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            alert("error");
        }
    });
});

$("#pathBtn2").bind("click", function () {
    var dstPath = $("#dstPath").val();
    dstPath = dstPath.replace(/\\/g,"/");
    var tempurl = "/setDstPath?dstPath="+dstPath;
    $.ajax({
        type: "post",
        url: tempurl,
        success: function (data) {
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            alert("error");
        }
    });
});

editor = new $.fn.dataTable.Editor( {
    ajax: "/setDBFCfg",
    table: "#datatable",
    fields: [ {
        label: "股东席位代码:",
        name: "code"
    }, {
        label: "DBF文件名称:",
        name: "name"
    }, {
        label: "DBF文件字段:",
        name: "field"
    }
    ],
    formOptions: {
        bubble: {
            title: 'Edit',
            buttons: false
        }
    }
} );

$('#datatable').on( 'click', 'tbody td', function (e) {
    if ( $(this).index() > 0 ) {
        editor.bubble( this );
    }
} );

$('#datatable').dataTable({
    dom: "Bfrtip",
    ajax: "/dbf.json",
    columns: [
        {
            data: null,
            defaultContent: '',
            className: 'select-checkbox',
            orderable: false
        },
        { data: "code" },
        { data: "name" },
        { data: "field" }
    ],
    order: [ 1, 'asc' ],
    select: {
        style:    'os',
        selector: 'td:first-child'
    },
    buttons: [
        { extend: "create", editor: editor },
        { extend: "remove", editor: editor }
    ]
});
