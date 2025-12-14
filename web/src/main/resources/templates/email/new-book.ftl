<#include "layout/header.ftl">

<h3 style="color: #667eea;">Нова книга додана до каталогу</h3>

<p>Назва: <b>${bookTitle}</b></p>
<p>Автор: <b>${bookAuthor}</b></p>

<#if bookYear??>
    <p>Рік видання: <b>${bookYear?c}</b></p>
    <#if (bookYear < 2000)>
        <p style="color:#7a4e00; background: #fff3cd; padding: 10px; border-radius: 5px;">
            <b>⭐ Раритетне видання!</b>
        </p>
    </#if>
</#if>

<#if bookIsbn??>
    <p>ISBN: <b>${bookIsbn}</b></p>
</#if>

<div style="text-align: center; margin: 30px 0;">
    <a href="http://localhost:8080/books"
       style="display: inline-block; background:#4CAF50; color:white; padding:12px 30px; text-decoration:none; border-radius:5px; font-weight: bold;">
        📖 Переглянути в каталозі
    </a>
</div>

<#include "layout/footer.ftl">