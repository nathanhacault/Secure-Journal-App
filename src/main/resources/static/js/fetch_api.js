/* Global variables */
let addForm, addDate, addText, addImage, addUrl, jsonForm, addJson;
let editForm, editDate, editText, editImage, editId, editUrl;
let entriesHtml;

const BASE_URL = location.origin + "/api/";

function initializeGlobalVariables() { //function to get all the document objects
    jsonForm = document.getElementById('addFormJSON');
    addForm = document.getElementById('addForm');
    addDate = document.getElementById('date');
    addText = document.getElementById('text');
    addImage = document.getElementById('image');
    addJson = document.getElementById('jsonU');
    addUrl = document.getElementById('url');
    editForm = document.getElementById('editForm');
    editDate = document.getElementById('edit-date');
    editText = document.getElementById('edit-text');
    editImage = document.getElementById('edit-image');
    editUrl = document.getElementById('edit-url');
    editId = document.getElementById('edit-id');
    entriesHtml = document.getElementById('entries');
}


window.addEventListener("load", () => { //function for when window loads, adds a listener
    initializeGlobalVariables();


    addForm.addEventListener('submit', (event) => { //add form listener
        event.preventDefault();
        $('#addModal').modal('hide');
        let img = addImage.files[0];
        let url = addUrl.value; //getting data from fields
        if (img != undefined) { //making sure img is not undefined
            var reader = new FileReader(); //read the image inputted
            reader.onload = ((e) => {
                let result = e.target.result;
                addEntry(addDate.value, addText.value, result); //call add entry function
                resetFields(addDate, addText, addImage, addUrl); //reset the add modal
            });
            reader.readAsDataURL(img);
        } else if (url != ""){ // check if a image url is added
            addEntry(addDate.value, addText.value, url); //call add entry function
            resetFields(addDate, addText, addImage, addUrl); //reset the add modal
        } else {
            addEntry(addDate.value, addText.value, ""); //call add entry function
            resetFields(addDate, addText, addImage, addUrl); //reset the add modal
        }
    });

    jsonForm.addEventListener('submit', (event) => {
        event.preventDefault();
        $('#jsonModal').modal('hide');

        let file = addJson.files[0]; //getting the file from the field

        if (file != undefined) { //making sure the file is not undefined
            var reader = new FileReader();
            reader.onload = onReaderLoad; //read the file
            reader.readAsText(file);

        }
    });
    function onReaderLoad(event){ //function for json form to read the json file
        console.log(event.target.result);
        var obj = JSON.parse(event.target.result); //parse the json file
        addJsonEntry(obj);
        resetFields(addJson);
    }


    editForm.addEventListener('submit', (event) => { //edit form listener
        event.preventDefault();
        $('#editModal').modal('hide');
        let img = editImage.files[0];
        let url = editUrl.value; //get data from fields
        if (img != undefined) { //making sure img is not undefined
            var reader = new FileReader(); //read the image
            reader.onload = ((e) => {
                let result = e.target.result;
                editEntry(editId.value, editDate.value, editText.value, result); //call edit function
                resetFields(editId, editDate, editText, editImage, editUrl); //call reset function
            });
            reader.readAsDataURL(img);
        } else if (url != ""){ //else make sure url field not empty
            editEntry(editId.value, editDate.value, editText.value, url); //call edit function
            resetFields(editId, editDate, editText, editImage, editUrl); //call reset function
        } else { //else proceed with blank img
            editEntry(editId.value, editDate.value, editText.value, ""); //call edit function
            resetFields(editId, editDate, editText, editImage, editUrl); //call reset function
        }
    });

});


function resetFields(...args) { //function to clear the fields
    args.forEach(argument => argument.value="");
}


function checkEmptyField(...args) { //function to verify if the fields are not empty.
    for (let i = 0; i < args.length; i++)
        if(args[i] === "") return true;
    return false;
}

function showEditEntry(element) { // Sets the edit fields and shows the edit modal
    setEditFields(element.parentNode.parentNode.parentNode);
    $('#editModal').modal('show');
}


function setEditFields(element) { // Sets all the fields for the edit modal
    editDate.value = element.children[0].children[1].innerText;
    editId.value = element.children[0].children[0].innerText;
    editText.value = element.children[1].children[0].innerText;
    let url = element.children[1].children[2].children[0].src;
    if (!url.startsWith("data:image")) {
        editUrl.value = url;
    }
}

/* ******* Fetching functions ******* */

/**
 *
 */
function getAll() { //Fetches all journal entries and generates them to the HTML doc
    $.ajax({
        url: BASE_URL + "entries",
        type: 'GET',
        contentType: "application/json",
        success: function(data) {
            updateEntries(data.data); //update the view
        },
        fail: (err) => console.log("Couldn't get contacts ", err)
    });
}


function addEntry(date, text, img) { //function to make call to add entry into the DB.
    if(checkEmptyField(date, text, img)) return; //make sure not empty
    let dataObject = { //create object to be passed
        date: date,
        text: text,
        img: img
    };
    console.log("Adding ", dataObject);
    $.ajax({
        url: BASE_URL + "entry/create",
        type: 'POST',
        contentType: "application/json",
        data: JSON.stringify(dataObject),
        success: function(data) {
            getAll(); //update the view
        },
        fail: (err) => console.log("Couldn't create contact " + dataObject, err)
    });
}

function addJsonEntry(obj) { //function to make call to add json object into the DB.
    if(checkEmptyField(obj)) return; //make sure not empty

    console.log("Adding ", obj);
    $.ajax({
        url: BASE_URL + "entry/create",
        type: 'POST',
        contentType: "application/json",
        data: JSON.stringify(obj),
        success: function(data) {
            getAll(); //update the view
        },
        fail: (err) => console.log("Couldn't create contact " + obj, err)
    });
}

function editEntry(id, date, text, img) { //function to make call to edit an existing entry
    if(checkEmptyField(id, date, text, img)) return; //make sure not empty
    let dataObject = {id: id, date: date, text: text, img:img}
    console.log("Editing ", dataObject);
    console.log("Date",dataObject.date);
    $.ajax({
        url: BASE_URL + "entry/update",
        type: 'PATCH',
        contentType: "application/json",
        data: JSON.stringify(dataObject),
        success: function(data) {
            getAll();
        },
        fail: (err) => console.log("Couldn't update contact " + dataObject, err)
    });
}

function deleteEntry(element) { //function to delete an entry
    let parent = element.parentNode.parentNode.parentNode
    let id = parent.children[0].children[0].innerText;
    console.log("Deleting ", id);
    console.log(location.protocol + '//' + location.host + "/api/entry/delete/"+id)
    $.ajax({
        url: BASE_URL + "entry/delete/" + id,
        type: 'DELETE',
        contentType: "application/json",
        success: function(data) {
            parent.parentNode.removeChild(parent); //remove entry from view
        },
        fail: (err) => console.log("Couldn't delete entry", err)
    });
}


function updateEntries(entries) { //function to update an entry
    entriesHtml.innerHTML = "";
    entries.forEach(entry => {
       entriesHtml.appendChild(generateEntry(entry));
    });
}
function downloadFile(element) { //file download function
    let parent = element.parentNode.parentNode.parentNode
    let id = parent.children[0].children[0].innerText;
    window.location.href = BASE_URL + "entry/download/"+id ;
}

function generateEntry(entry) {  //function to generate a journal entry for the view
    const entryHtml = `
        <div class="entry-header card-header">
            <div class="entry-id"><h2>${entry.id}</h2></div>
            <div class="entry-date"><h2>${entry.date}</h2></div>
            <div class="btn-group">
                <button class="btn btn-sm" onclick="showEditEntry(this);">
                    <span class="oi oi-pencil" title="pencil" aria-hidden="true"></span>
                </button>
                <button class="btn btn-sm" onclick="downloadFile(this);">
                        <span class="material-symbols--download" title="pencil" aria-hidden="true"></span>
                 </button>
                <button class="close" onclick="deleteEntry(this);">x</button>
            </div>
        </div>
        <div class="entry-body card-body">
            <div class="entry-text">${entry.text}</div>
            <br>
            <div class="entry-image">
                <img class="card-img-top" src="${entry.img}" alt="entry-img"/>
            </div>
        </div>
        <div class="clear"></div>`;

    let entryDiv = document.createElement('div');
    entryDiv.setAttribute('class', 'entry card');
    entryDiv.innerHTML = entryHtml;
    return entryDiv;
}
