<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <style>
        body {
            font-family: Arial, sans-serif;
            line-height: 1.6;
            color: #333;
        }
        .container {
            max-width: 600px;
            margin: 0 auto;
            padding: 20px;
            background-color: #f9f9f9;
        }
        .header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 30px;
            text-align: center;
            border-radius: 10px 10px 0 0;
        }
        .content {
            background: white;
            padding: 30px;
            border-radius: 0 0 10px 10px;
        }
        .button {
            display: inline-block;
            padding: 15px 30px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            text-decoration: none;
            border-radius: 8px;
            margin: 20px 0;
            font-weight: bold;
        }
        .footer {
            text-align: center;
            margin-top: 20px;
            color: #666;
            font-size: 0.9em;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>📚 Книжковий каталог</h1>
            <p>Підтвердження реєстрації</p>
        </div>
        <div class="content">
            <h2>Вітаємо, ${username}! 🎉</h2>
            <p>Дякуємо за реєстрацію в нашому Книжковому каталозі!</p>
            <p>Для активації вашого акаунту, будь ласка, натисніть на кнопку нижче:</p>

            <div style="text-align: center;">
                <a href="${confirmationUrl}" class="button">
                    ✅ Підтвердити email
                </a>
            </div>

            <p>Або скопіюйте це посилання в браузер:</p>
            <p style="background: #f0f0f0; padding: 10px; border-radius: 5px; word-break: break-all;">
                ${confirmationUrl}
            </p>

            <p><strong>Важливо:</strong> Посилання дійсне протягом 24 годин.</p>

            <p>Якщо ви не реєструвалися на нашому сайті, проігноруйте цей лист.</p>
        </div>
        <div class="footer">
            <p>З повагою, команда Книжкового каталогу</p>
            <p>Це автоматичне повідомлення, не відповідайте на нього.</p>
        </div>
    </div>
</body>
</html>