#include <stdio.h>
#include <dlfcn.h>
#include <stdlib.h>
#include <string.h>
#include <inttypes.h>
#include <android/log.h>
#include <fcntl.h>
#include <jni.h>

#define BYTEHOOK_STATUS_CODE_OK                  0
#define BYTEHOOK_STATUS_CODE_UNINIT              1
#define BYTEHOOK_STATUS_CODE_INITERR_INVALID_ARG 2
#define BYTEHOOK_STATUS_CODE_INITERR_SYM         3
#define BYTEHOOK_STATUS_CODE_INITERR_TASK        4
#define BYTEHOOK_STATUS_CODE_INITERR_HOOK        5
#define BYTEHOOK_STATUS_CODE_INITERR_ELF         6
#define BYTEHOOK_STATUS_CODE_INITERR_ELF_REFR    7
#define BYTEHOOK_STATUS_CODE_INITERR_TRAMPO      8
#define BYTEHOOK_STATUS_CODE_INITERR_SIG         9
#define BYTEHOOK_STATUS_CODE_INITERR_DLMTR       10
#define BYTEHOOK_STATUS_CODE_INVALID_ARG         11
#define BYTEHOOK_STATUS_CODE_UNMATCH_ORIG_FUNC   12
#define BYTEHOOK_STATUS_CODE_NOSYM               13
#define BYTEHOOK_STATUS_CODE_GET_PROT            14
#define BYTEHOOK_STATUS_CODE_SET_PROT            15
#define BYTEHOOK_STATUS_CODE_SET_GOT             16
#define BYTEHOOK_STATUS_CODE_NEW_TRAMPO          17
#define BYTEHOOK_STATUS_CODE_APPEND_TRAMPO       18
#define BYTEHOOK_STATUS_CODE_GOT_VERIFY          19
#define BYTEHOOK_STATUS_CODE_REPEATED_FUNC       20
#define BYTEHOOK_STATUS_CODE_READ_ELF            21
#define BYTEHOOK_STATUS_CODE_CFI_HOOK_FAILED     22
#define BYTEHOOK_STATUS_CODE_ORIG_ADDR           23
#define BYTEHOOK_STATUS_CODE_INITERR_CFI         24
#define BYTEHOOK_STATUS_CODE_IGNORE              25
#define BYTEHOOK_STATUS_CODE_MAX                 255

#define BYTEHOOK_MODE_AUTOMATIC 0
#define BYTEHOOK_MODE_MANUAL    1

#ifdef __cplusplus
extern "C" {
#endif

const char *(*bytehook_get_version)(void);

typedef void *bytehook_stub_t;

typedef void (*bytehook_hooked_t)(bytehook_stub_t task_stub, int status_code, const char *caller_path_name,
                                  const char *sym_name, void *new_func, void *prev_func, void *arg);

typedef bool (*bytehook_caller_allow_filter_t)(const char *caller_path_name, void *arg);

int (*bytehook_init)(int mode, bool debug);

bytehook_stub_t (*bytehook_hook_single)(const char *caller_path_name, const char *callee_path_name,
                                     const char *sym_name, void *new_func, bytehook_hooked_t hooked,
                                     void *hooked_arg);

bytehook_stub_t (*bytehook_hook_partial)(bytehook_caller_allow_filter_t caller_allow_filter,
                                      void *caller_allow_filter_arg, const char *callee_path_name,
                                      const char *sym_name, void *new_func, bytehook_hooked_t hooked,
                                      void *hooked_arg);

bytehook_stub_t (*bytehook_hook_all)(const char *callee_path_name, const char *sym_name, void *new_func,
                                  bytehook_hooked_t hooked, void *hooked_arg);

int (*bytehook_unhook)(bytehook_stub_t stub);

int (*bytehook_add_ignore)(const char *caller_path_name);

int (*bytehook_get_mode)(void);
bool (*bytehook_get_debug)(void);
void (*bytehook_set_debug)(bool debug);
bool (*bytehook_get_recordable)(void);
void (*bytehook_set_recordable)(bool recordable);

// get operation records
#define BYTEHOOK_RECORD_ITEM_ALL             0xFF  // 0b11111111
#define BYTEHOOK_RECORD_ITEM_TIMESTAMP       (1 << 0)
#define BYTEHOOK_RECORD_ITEM_CALLER_LIB_NAME (1 << 1)
#define BYTEHOOK_RECORD_ITEM_OP              (1 << 2)
#define BYTEHOOK_RECORD_ITEM_LIB_NAME        (1 << 3)
#define BYTEHOOK_RECORD_ITEM_SYM_NAME        (1 << 4)
#define BYTEHOOK_RECORD_ITEM_NEW_ADDR        (1 << 5)
#define BYTEHOOK_RECORD_ITEM_ERRNO           (1 << 6)
#define BYTEHOOK_RECORD_ITEM_STUB            (1 << 7)
char *(*bytehook_get_records)(uint32_t item_flags);
void (*bytehook_dump_records)(int fd, uint32_t item_flags);

// for internal use
void *(*bytehook_get_prev_func)(void *func);

// for internal use
void (*bytehook_pop_stack)(void *return_address);

// for internal use
void *(*bytehook_get_return_address)(void);

typedef void (*bytehook_pre_dlopen_t)(const char *filename, void *data);

typedef void (*bytehook_post_dlopen_t)(const char *filename,
                                       int result,  // 0: OK  -1: Failed
                                       void *data);

void (*bytehook_add_dlopen_callback)(bytehook_pre_dlopen_t pre, bytehook_post_dlopen_t post, void *data);

void (*bytehook_del_dlopen_callback)(bytehook_pre_dlopen_t pre, bytehook_post_dlopen_t post, void *data);

#ifdef __cplusplus
}
#endif

// call previous function in hook-function
#ifdef __cplusplus
#define BYTEHOOK_CALL_PREV(func, ...) ((decltype(&(func)))bytehook_get_prev_func((void *)(func)))(__VA_ARGS__)
#else
#define BYTEHOOK_CALL_PREV(func, func_sig, ...) \
  ((func_sig)bytehook_get_prev_func((void *)(func)))(__VA_ARGS__)
#endif

// get return address in hook-function
#define BYTEHOOK_RETURN_ADDRESS()                                                          \
  ((void *)(BYTEHOOK_MODE_AUTOMATIC == bytehook_get_mode() ? bytehook_get_return_address() \
                                                           : __builtin_return_address(0)))

// pop stack in hook-function (for C/C++)
#define BYTEHOOK_POP_STACK()                                                                             \
  do {                                                                                                   \
    if (BYTEHOOK_MODE_AUTOMATIC == bytehook_get_mode()) bytehook_pop_stack(__builtin_return_address(0)); \
  } while (0)

// pop stack in hook-function (for C++ only)
#ifdef __cplusplus
class BytehookStackScope {
public:
    BytehookStackScope(void *return_address) : return_address_(return_address) {}

    ~BytehookStackScope() {
        if (BYTEHOOK_MODE_AUTOMATIC == bytehook_get_mode()) bytehook_pop_stack(return_address_);
    }

private:
    void *return_address_;
};
#define BYTEHOOK_STACK_SCOPE() BytehookStackScope bytehook_stack_scope_obj(__builtin_return_address(0))
#endif

#define LOG(fmt, ...) __android_log_print(ANDROID_LOG_INFO, "HACKER_TAG", fmt, ##__VA_ARGS__)

static void debug(const char *sym, const char *pathname, int flags, int fd, void *lr) {
    Dl_info info;
    memset(&info, 0, sizeof(info));
    dladdr(lr, &info);

    LOG("proxy %s(\"%s\", %d), return FD: %d, called from: %s (%s)", sym, pathname, flags, fd, info.dli_fname,
        info.dli_sname);
}


// get return address in hook-function
#define BYTEHOOK_RETURN_ADDRESS()                                                          \
  ((void *)(BYTEHOOK_MODE_AUTOMATIC == bytehook_get_mode() ? bytehook_get_return_address() \
                                                           : __builtin_return_address(0)))
#define BYTEHOOK_STATUS_CODE_ORIG_ADDR           23

// pop stack in hook-function (for C/C++)
#define BYTEHOOK_POP_STACK()                                                                             \
  do {                                                                                                   \
    if (BYTEHOOK_MODE_AUTOMATIC == bytehook_get_mode()) bytehook_pop_stack(__builtin_return_address(0)); \
  } while (0)

typedef int (*open_t)(const char *, int, mode_t);
#define OPEN_DEF(fn)                                                                                         \
  static fn##_t fn##_prev = NULL;                                                                            \
  static bytehook_stub_t fn##_stub = NULL;                                                                   \
  static void fn##_hooked_callback(bytehook_stub_t task_stub, int status_code, const char *caller_path_name, \
                                   const char *sym_name, void *new_func, void *prev_func, void *arg) {       \
    if (BYTEHOOK_STATUS_CODE_ORIG_ADDR == status_code) {                                                     \
      fn##_prev = (fn##_t)prev_func;                                                                         \
      LOG(">>>>> save original address: %" PRIxPTR, (uintptr_t)prev_func);                                   \
    } else {                                                                                                 \
      LOG(">>>>> hooked. stub: %" PRIxPTR                                                                    \
          ", status: %d, caller_path_name: %s, sym_name: %s, new_func: %" PRIxPTR ", prev_func: %" PRIxPTR   \
          ", arg: %" PRIxPTR,                                                                                \
          (uintptr_t)task_stub, status_code, caller_path_name, sym_name, (uintptr_t)new_func,                \
          (uintptr_t)prev_func, (uintptr_t)arg);                                                             \
    }                                                                                                        \
  }
OPEN_DEF(open)

#define BYTEHOOK_CALL_PREV(func, func_sig, ...) \
  ((func_sig)bytehook_get_prev_func((void *)(func)))(__VA_ARGS__)


static int open_proxy_auto(const char *pathname, int flags, mode_t modes) {
    BYTEHOOK_STACK_SCOPE();
    int fd = BYTEHOOK_CALL_PREV(open_proxy_auto, open_t, pathname, flags, modes);
    Dl_info info;
    memset(&info, 0, sizeof(info));
    dladdr(BYTEHOOK_RETURN_ADDRESS(), &info);
    LOG("proxy %s(\"%s\", %d), return FD: %d, called from: %s (%s)", "open", pathname, flags, fd, info.dli_fname,
        info.dli_sname);
    return fd;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_vlite_app_sample_BHookSample_hookAndTestOpenat(JNIEnv *env, jclass clazz, jlong bhook_ptr) {
    void** bhookFuncs = (void**)bhook_ptr;
    bytehook_get_version = (typeof(bytehook_get_version))bhookFuncs[0];
    bytehook_init = (typeof(bytehook_init))bhookFuncs[1];
    bytehook_hook_single = (typeof(bytehook_hook_single))bhookFuncs[2];
    bytehook_hook_partial = (typeof(bytehook_hook_partial))bhookFuncs[3];
    bytehook_hook_all = (typeof(bytehook_hook_all))bhookFuncs[4];
    bytehook_unhook = (typeof(bytehook_unhook))bhookFuncs[5];
    bytehook_add_ignore = (typeof(bytehook_add_ignore))bhookFuncs[6];
    bytehook_get_mode = (typeof(bytehook_get_mode))bhookFuncs[7];
    bytehook_get_debug = (typeof(bytehook_get_debug))bhookFuncs[8];
    bytehook_set_debug = (typeof(bytehook_set_debug))bhookFuncs[9];
    bytehook_get_recordable = (typeof(bytehook_get_recordable))bhookFuncs[10];
    bytehook_set_recordable = (typeof(bytehook_set_recordable))bhookFuncs[11];
    bytehook_get_records = (typeof(bytehook_get_records))bhookFuncs[12];
    bytehook_dump_records = (typeof(bytehook_dump_records))bhookFuncs[13];
    bytehook_get_prev_func = (typeof(bytehook_get_prev_func))bhookFuncs[14];
    bytehook_pop_stack = (typeof(bytehook_pop_stack))bhookFuncs[15];
    bytehook_get_return_address = (typeof(bytehook_get_return_address))bhookFuncs[16];
    bytehook_add_dlopen_callback = (typeof(bytehook_add_dlopen_callback))bhookFuncs[17];
    bytehook_del_dlopen_callback = (typeof(bytehook_del_dlopen_callback))bhookFuncs[18];

    bytehook_set_debug(true);
    bytehook_hook_all(NULL, "open", (void*)&open_proxy_auto, open_hooked_callback, (void*)123);
}