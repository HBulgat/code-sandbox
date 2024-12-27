const readline = require('readline');
const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
});
rl.question("请输入内容：", (answer) => {
    console.log("你输入的是：", answer);
    rl.close();
});
