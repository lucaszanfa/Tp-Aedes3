package View;

public final class HtmlView {

    private HtmlView() {
    }

    public static String css() {
        return """
            :root {
                --bg: #f5f7fb;
                --panel: #ffffff;
                --ink: #17223b;
                --muted: #4a5a78;
                --accent: #0f766e;
                --border: #d6deed;
            }
            * { box-sizing: border-box; }
            body {
                margin: 0;
                font-family: "Trebuchet MS", Verdana, sans-serif;
                background: radial-gradient(circle at top right, #e5f6f3, var(--bg) 40%);
                color: var(--ink);
            }
            .shell {
                max-width: 1080px;
                margin: 20px auto;
                padding: 0 16px 20px;
            }
            h1, h2 { margin: 8px 0; }
            nav {
                display: flex;
                gap: 10px;
                flex-wrap: wrap;
                margin-bottom: 16px;
            }
            nav a {
                text-decoration: none;
                padding: 8px 12px;
                border-radius: 10px;
                background: var(--panel);
                border: 1px solid var(--border);
                color: var(--ink);
            }
            .grid {
                display: grid;
                grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
                gap: 14px;
            }
            .card {
                background: var(--panel);
                border: 1px solid var(--border);
                border-radius: 14px;
                padding: 14px;
            }
            label { display: block; margin: 8px 0 3px; font-weight: bold; }
            input {
                width: 100%;
                border: 1px solid var(--border);
                border-radius: 8px;
                padding: 8px;
            }
            button {
                margin-top: 10px;
                border: 0;
                border-radius: 9px;
                background: var(--accent);
                color: white;
                padding: 8px 11px;
                cursor: pointer;
            }
            table {
                width: 100%;
                border-collapse: collapse;
                margin-top: 10px;
                font-size: 14px;
            }
            th, td {
                border-bottom: 1px solid var(--border);
                text-align: left;
                padding: 8px 6px;
            }
            .msg {
                border: 1px solid #f2cf66;
                background: #fff8de;
                border-radius: 10px;
                padding: 8px;
                margin-bottom: 10px;
            }
            """;
    }

    public static String page(String title, String content) {
        return """
            <!doctype html>
            <html lang='pt-BR'>
            <head>
              <meta charset='utf-8'>
              <meta name='viewport' content='width=device-width,initial-scale=1'>
              <title>""" + title + """
              </title>
              <link rel='stylesheet' href='/styles.css'>
            </head>
            <body>
              <div class='shell'>
                """ + content + """
              </div>
            </body>
            </html>
            """;
    }

    public static String nav() {
        return """
            <nav>
              <a href='/'>Inicio</a>
              <a href='/clientes'>Clientes</a>
              <a href='/produtos'>Produtos</a>
              <a href='/cupons'>Cupons</a>
              <a href='/pedidos'>Pedidos</a>
            </nav>
            """;
    }

    public static String msgBox(String msg, String escaped) {
        if (msg == null || msg.isBlank()) {
            return "";
        }
        return "<div class='msg'>" + escaped + "</div>";
    }
}
