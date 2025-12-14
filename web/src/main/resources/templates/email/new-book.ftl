<#include "layout/header.ftl">

<h3 style="color: #667eea;">Нова книга додана до каталогу</h3>

<p>Назва: <b>${title}</b></p>
<p>Автор: <b>${author}</b></p>

<#if year??>
    <p>Рік видання: <b>${year?c}</b></p>
    <#if (year < 2000)>
        <p style="color:#7a4e00; background: #fff3cd; padding: 10px; border-radius: 5px;">
            <b>⭐ Раритетне видання!</b>
        </p>
    </#if>
</#if>

<#if comments?? && (comments?size > 0)>
    <p>Коментарі:</p>
    <ul>
        <#list comments as c>
            <li>${c}</li>
        </#list>
    </ul>
</#if>

<div style="text-align: center; margin: 30px 0;">
    <a href="http://localhost:8080/books/${id?c}"
       style="display: inline-block; background:#4CAF50; color:white; padding:12px 30px; text-decoration:none; border-radius:5px; font-weight: bold;">
        📖 Переглянути книгу
    </a>
</div>

<p style="color: #666; font-size: 14px;">
    Дата додавання: ${createdAt?string("dd.MM.yyyy HH:mm")}
</p>

<#include "layout/footer.ftl">