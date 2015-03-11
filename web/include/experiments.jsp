<h2>CraTwitter</h2>
<form class="crawcial-login-form" action="twitter" method="post">
    <div class="crawcial-form-row">
        <input class="crawcial-form-large" type="text" name="terms"
               value=""
               placeholder="Filter terms" autofocus="">
    </div>
    <div class="crawcial-form-row">
        <input class="crawcial-form-large" type="text" name="duration"
               value=""
               placeholder="Duration">
    </div>
    <div class="crawcial-form-row">
        <button>Go</button>
    </div>
    <input type="hidden" name="action" value="persist">
</form>
<h2>Create new user</h2>
<form class="crawcial-login-form" action="user" method="post">
    <div class="crawcial-form-row">
        <input class="crawcial-form-large" type="text" name="user"
               value=""
               placeholder="Username" autofocus="">
    </div>
    <div class="crawcial-form-row">
        <input class="crawcial-form-large" type="password" name="password"
               value=""
               placeholder="Password">
    </div>
    <div class="crawcial-form-row">
        <button>Create new user</button>
    </div>
    <input type="hidden" name="action" value="adduser">

</form>
<form class="crawcial-login-form" action="user" method="post">
    <div class="crawcial-form-row">
        <button>Delete current user</button>
    </div>
    <input type="hidden" name="action" value="deluser">
</form>